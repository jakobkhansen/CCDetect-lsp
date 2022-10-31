package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ai.serenade.treesitter.TreeCursor;

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
    int[] SA, ISA, LCP;

    @Override
    public List<CodeClone> getClones() {
        // for (CodeClone clone : clones) {
        // LOGGER.info("clone: " + clone.toString());
        // }
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        LOGGER.info("onIndexChange TreesitterDetector");
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

        int[] fingerprint = Ints.toArray(fullFingerprint);

        // Build suffix, inverse, lcp
        Timer timer = new Timer();
        timer.start();
        ExtendedSuffixArray suff = new SAIS().buildExtendedSuffixArray(fingerprint);
        timer.stop();
        timer.log("Time to build suffix array, inverse and lcp");
        SA = suff.getSuffix();
        ISA = suff.getInverseSuffix();
        LCP = suff.getLcp();
        int[] indices = new int[fingerprint.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        // LOGGER.info("Indices: " + Printer.print(indices));
        // LOGGER.info("Fingerprint: " + Printer.print(fingerprint));
        // LOGGER.info("Suffix: " + Printer.print(suff.getSuffix()));
        // LOGGER.info("Inverse: " + Printer.print(suff.getInverseSuffix()));
        // LOGGER.info("LCP: " + Printer.print(suff.getLcp()));

        int[] cloneIndices = extractCloneIndicesFromSA();
        LOGGER.info("Clone indices: " + Printer.print(cloneIndices));

        RangeConverter converter = new RangeConverter();
        Map<Integer, CodeClone> cloneMap = new HashMap<>();
        for (int i = 0; i < cloneIndices.length; i++) {
            int firstIndex = cloneIndices[i];
            int secondIndex = SA[ISA[cloneIndices[i]] - 1];

            int cloneSize = LCP[ISA[firstIndex]] - 1;

            TokenSourcePair first = getTokenSourcePairFromIndex(firstIndex, cloneSize);
            TokenSourcePair second = getTokenSourcePairFromIndex(secondIndex, cloneSize);

            if (first.getRangeBetween() == null || second.getRangeBetween() == null) {
                continue;
            }

            CodeClone firstClone = cloneMap.getOrDefault(firstIndex,
                    new CodeClone(first.getUri(), converter.convertFromRight(first.getRangeBetween())));

            CodeClone secondClone = cloneMap.getOrDefault(secondIndex,
                    new CodeClone(second.getUri(), converter.convertFromRight(second.getRangeBetween())));

            cloneMap.put(firstIndex, firstClone);
            cloneMap.put(secondIndex, secondClone);
            CodeClone.addMatch(firstClone, secondClone);
        }
        for (CodeClone clone : cloneMap.values()) {
            clones.add(clone);
        }
    }

    private int[] extractCloneIndicesFromSA() {
        Configuration config = Configuration.getInstance();
        int cloneThreshold = config.getCloneTokenThreshold();
        // Fetch clones, ignore contained clones
        ArrayList<Integer> clones = new ArrayList<>();
        LOGGER.info("Threshold: " + cloneThreshold);
        for (int i = 1; i < SA.length; i++) {

            if (LCP[ISA[i]] >= cloneThreshold) {
                // Ignore contained clones
                clones.add(i);
                i++;
                while (i < SA.length &&
                        LCP[ISA[i - 1]] > LCP[ISA[i]] &&
                        LCP[ISA[i]] >= cloneThreshold) {
                    i++;
                }
            }
        }
        return Ints.toArray(clones);
    }

    private TokenSourcePair getTokenSourcePairFromIndex(int index, int cloneSize) {

        TokenSource left = sourceMap.getSource(index);
        TokenSource right = sourceMap.getSource(index + cloneSize);

        return new TokenSourcePair(left, right);

    }

    private FingerprintIndex buildFingerprintIndex(DocumentIndex<TreesitterDocumentModel> index) {
        FingerprintIndex fingerprintIndex = new FingerprintIndex();
        Configuration config = Configuration.getInstance();
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

                if (filterRecursiveChildren(matchNode)) {
                    continue;
                }

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

    private boolean filterRecursiveChildren(Node node) {

        Node current = node.getParent();
        while (current != null) {
            if (current.getType().equals(node.getType())) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }
}
