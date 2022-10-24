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

    @Override
    public List<CodeClone> getClones() {
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        fingerprintIndex = buildFingerprintIndex(index);

        // Build fingerprint
        ArrayList<Integer> fullFingerprint = new ArrayList<>();
        for (Fingerprint f : fingerprintIndex.fingerprints) {
            TSRange[] ranges = f.getRanges();
            int[] fingerprint = f.getFingerprint();
            LOGGER.info("range size: " + ranges.length);
            LOGGER.info("fingerprint size: " + fingerprint.length);
            for (int i = 0; i < fingerprint.length; i++) {
                int tokenValue = fingerprint[i];
                fullFingerprint.add(tokenValue);
                sourceMap.put(f.getUri(), ranges[i]);
            }
        }
        // 0 terminal
        fullFingerprint.add(0);
        sourceMap.put(null, null);

        LOGGER.info("fullFingerprint size: " + fullFingerprint.size());
        LOGGER.info("sourceMap size: " + sourceMap.size());
        int[] fingerprint = Ints.toArray(fullFingerprint);

        // Build suffix, inverse, lcp
        ExtendedSuffixArray suff = new SAIS().buildExtendedSuffixArray(fingerprint);
        int[] SA = suff.getSuffix();
        int[] ISA = suff.getInverseSuffix();
        int[] LCP = suff.getLcp();
        LOGGER.info("Fingerprint: " + Printer.print(fingerprint));
        LOGGER.info("Suffix: " + Printer.print(suff.getSuffix()));
        LOGGER.info("LCP: " + Printer.print(suff.getLcp()));

        int[] cloneIndices = extractClonesFromSA(SA, ISA, LCP);

        TokenSourcePair[] clones = new TokenSourcePair[cloneIndices.length];
        for (int i = 0; i < cloneIndices.length; i++) {
            int cloneIndex = cloneIndices[i];
            int cloneSize = LCP[ISA[cloneIndex]];

            if (fingerprint[cloneIndex + cloneSize] == 1) {
                cloneSize--;
            }

            TokenSource left = sourceMap.getSource(cloneIndex);
            TokenSource right = sourceMap.getSource(cloneIndex + cloneSize);
            LOGGER.info(Printer.print(left.getRange()));
            LOGGER.info(Printer.print(right.getRange()));

            clones[i] = new TokenSourcePair(left, right);
            LOGGER.info("clone: " + clones[i].getUri());
            LOGGER.info(Printer.print(clones[i].getRangeBetween()));
        }
    }

    private int[] extractClonesFromSA(int[] SA, int[] ISA, int[] LCP) {
        // Fetch clones, ignore contained clones
        ArrayList<Integer> clones = new ArrayList<>();
        int cloneCount = 0;
        for (int i = 0; i < SA.length; i++) {

            if (LCP[ISA[i]] >= CLONE_THRESHOLD) {
                // Ignore contained clones
                clones.add(i);
                while (LCP[ISA[i]] > LCP[ISA[i + 1]] && LCP[ISA[i]] >= CLONE_THRESHOLD) {
                    i++;
                }
            }
        }
        LOGGER.info("Num clones: " + cloneCount);
        return Ints.toArray(clones);
    }

    private FingerprintIndex buildFingerprintIndex(DocumentIndex<TreesitterDocumentModel> index) {
        FingerprintIndex fingerprintIndex = new FingerprintIndex();
        Configuration config = Configuration.getInstance();
        LOGGER.info(Printer.print(config.getIgnoreNodes()));
        Timer timer = new Timer();
        timer.start();
        for (TreesitterDocumentModel document : index) {
            LOGGER.info("Fetching tokens for " + document.getUri());
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
