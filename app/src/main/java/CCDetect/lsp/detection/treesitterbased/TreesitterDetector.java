package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.utils.RangeConverter;
import CCDetect.lsp.utils.Timer;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSQueryCursor;
import ai.serenade.treesitter.TSQueryMatch;
import ai.serenade.treesitter.TSRange;

/**
 * TreesitterDetector
 */
public class TreesitterDetector implements CloneDetector<TreesitterDocumentModel> {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);
    List<CodeClone> clones = new ArrayList<>();
    TreesitterFingerprintGenerator fingerprintGenerator = new TreesitterFingerprintGenerator();
    FingerprintIndex fingerprintIndex;
    TokenSourceMap sourceMap = new TokenSourceMap();
    int CLONE_THRESHOLD = 50;
    int[] SA, ISA, LCP;

    @Override
    public List<CodeClone> getClones() {
        for (CodeClone clone : clones) {
            LOGGER.info(clone.toString());
        }
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        clones = new ArrayList<>();
        fingerprintIndex = buildFingerprintIndex(index);

        // Build fingerprint
        ArrayList<Integer> fullFingerprint = new ArrayList<>();
        for (Fingerprint f : fingerprintIndex.fingerprints) {
            TSRange[] ranges = f.getRanges();
            int[] fingerprint = f.getFingerprint();
            for (int i = 0; i < fingerprint.length; i++) {
                int tokenValue = fingerprint[i];
                fullFingerprint.add(tokenValue);
                sourceMap.put(f.getUri(), ranges[i]);
            }
        }
        // 0 terminal
        fullFingerprint.add(0);
        sourceMap.put(null, null);

        LOGGER.info("Hashes: " + Printer.print(fingerprintGenerator.getTokenToCharMap()));
        int[] fingerprint = Ints.toArray(fullFingerprint);

        // Build suffix, inverse, lcp
        ExtendedSuffixArray suff = new SAIS().buildExtendedSuffixArray(fingerprint);
        SA = suff.getSuffix();
        ISA = suff.getInverseSuffix();
        LCP = suff.getLcp();
        int[] indices = new int[fingerprint.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        LOGGER.info("Indices:     " + Printer.print(indices));
        LOGGER.info("Fingerprint: " + Printer.print(fingerprint));
        LOGGER.info("Suffix:      " + Printer.print(suff.getSuffix()));
        LOGGER.info("Inverse:     " + Printer.print(suff.getInverseSuffix()));
        LOGGER.info("LCP:         " + Printer.print(suff.getLcp()));

        int[] cloneIndices = extractCloneIndicesFromSA();
        LOGGER.info("Clone indices: " + Printer.print(cloneIndices));

        for (int i = 0; i < cloneIndices.length; i++) {
            int firstIndex = cloneIndices[i];
            int secondIndex = SA[ISA[cloneIndices[i]] - 1];

            int cloneSize = LCP[ISA[firstIndex]] - 1;

            TokenSourcePair first = getTokenSourcePairFromIndex(firstIndex, fingerprint, cloneSize);
            TokenSourcePair second = getTokenSourcePairFromIndex(secondIndex, fingerprint, cloneSize);
            buildCodeClonesFromSourcePairs(first, second);
        }
    }

    private int[] extractCloneIndicesFromSA() {
        // Fetch clones, ignore contained clones
        ArrayList<Integer> clones = new ArrayList<>();
        for (int i = 0; i < SA.length; i++) {

            if (LCP[ISA[i]] >= CLONE_THRESHOLD) {
                // Ignore contained clones
                clones.add(i);
                while (LCP[ISA[i]] > LCP[ISA[i + 1]] && LCP[ISA[i]] >= CLONE_THRESHOLD) {
                    i++;
                }
            }
        }
        return Ints.toArray(clones);
    }

    private TokenSourcePair getTokenSourcePairFromIndex(int index, int[] fingerprint, int cloneSize) {

        TokenSource left = sourceMap.getSource(index);
        TokenSource right = sourceMap.getSource(index + cloneSize);

        return new TokenSourcePair(left, right);

    }

    private void buildCodeClonesFromSourcePairs(TokenSourcePair first, TokenSourcePair second) {
        RangeConverter converter = new RangeConverter();
        CodeClone clone = new CodeClone(first.getUri(), converter.convertFromRight(first.getRangeBetween()));
        CodeClone match = new CodeClone(second.getUri(), converter.convertFromRight(second.getRangeBetween()));
        CodeClone.addMatch(clone, match);

        clones.add(clone);
        clones.add(match);
    }

    private FingerprintIndex buildFingerprintIndex(DocumentIndex<TreesitterDocumentModel> index) {
        FingerprintIndex fingerprintIndex = new FingerprintIndex();
        Configuration config = Configuration.getInstance();
        LOGGER.info(Printer.print(config.getIgnoreNodes()));
        Timer timer = new Timer();
        timer.start();
        for (TreesitterDocumentModel document : index) {
            document.buildTree();

            Node root = document.getAST().getTree().getRootNode();
            String query = config.getFragmentQuery();

            TSQueryCursor methodsQueryCursor = TreeSitterLibrary.queryPattern(root,
                    query);

            if (methodsQueryCursor == null) {
                LOGGER.info("Invalid pattern");
                return fingerprintIndex;
            }

            for (TSQueryMatch match = methodsQueryCursor.nextMatch(); match != null; match = methodsQueryCursor
                    .nextMatch()) {
                Node matchNode = match.getCaptures()[0].getNode();

                Fingerprint fingerprint = fingerprintGenerator.getFingerprint(document.getText(),
                        document.getUri(), matchNode);

                fingerprintIndex.add(fingerprint);
            }
            document.freeTree();
        }
        timer.stop();
        timer.log("Time to fetch tokens");

        return fingerprintIndex;
    }
}
