package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.detection.treesitterbased.fingerprint.TreesitterFingerprintGenerator;
import CCDetect.lsp.detection.treesitterbased.sourcemap.TokenSource;
import CCDetect.lsp.detection.treesitterbased.sourcemap.TokenSourceMap;
import CCDetect.lsp.detection.treesitterbased.sourcemap.TokenSourcePair;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.server.NotificationHandler;
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
    List<CodeClone> clones;
    TreesitterFingerprintGenerator fingerprintGenerator = new TreesitterFingerprintGenerator();
    TokenSourceMap sourceMap;
    ExtendedSuffixArray eSuff;
    // int[] SA, ISA, LCP;

    @Override
    public List<CodeClone> getClones() {
        LOGGER.info("clones: " + clones.size());
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        LOGGER.info("onIndexChange TreesitterDetector");

        Timer timerIndexChange = new Timer();
        timerIndexChange.start();
        clones = new ArrayList<>();
        sourceMap = new TokenSourceMap();

        buildFingerprints(index);
        // Build fingerprint
        ArrayList<Integer> fullFingerprint = new ArrayList<>();
        for (TreesitterDocumentModel doc : index) {
            for (Fingerprint f : doc.getFingerprint()) {

                TSRange[] ranges = f.getRanges();
                int[] fingerprint = f.getFingerprint();
                for (int i = 0; i < fingerprint.length; i++) {
                    int tokenValue = fingerprint[i];
                    fullFingerprint.add(tokenValue);
                    sourceMap.put(f.getUri(), ranges[i]);
                }
            }
        }
        // 0 terminal
        fullFingerprint.add(0);
        sourceMap.put(null, null);

        int[] fingerprint = Ints.toArray(fullFingerprint);

        // Build suffix, inverse, lcp
        NotificationHandler.startNotification("clones", "Finding clones");
        Timer timer = new Timer();
        timer.start();
        eSuff = new SAIS().buildExtendedSuffixArray(fingerprint);
        timer.stop();
        timer.log("Time to build suffix array, inverse and lcp");

        // Only for logging
        // int[] indices = new int[fingerprint.length];
        // for (int i = 0; i < indices.length; i++) {
        // indices[i] = i;
        // }
        // LOGGER.info("Indices: " + Printer.print(indices));
        // LOGGER.info("Fingerprint: " + Printer.print(fingerprint));
        // LOGGER.info("Suffix: " + Printer.print(suff.getSuffix()));
        // LOGGER.info("Inverse: " + Printer.print(suff.getInverseSuffix()));
        // LOGGER.info("LCP: " + Printer.print(suff.getLcp()));

        int[] cloneIndices = extractCloneIndicesFromSA();
        LOGGER.info("Clone indices: " + Printer.print(cloneIndices));

        Map<Integer, CodeClone> cloneMap = getClones(cloneIndices);

        for (CodeClone clone : cloneMap.values()) {
            clones.add(clone);
        }

        timerIndexChange.stop();
        timerIndexChange.log("indexDidChange time");
        NotificationHandler.endNotification("clones", clones.size() + " clones found");
    }

    private Map<Integer, CodeClone> getClones(int[] cloneIndices) {
        RangeConverter converter = new RangeConverter();
        Map<Integer, CodeClone> cloneMap = new HashMap<>();
        for (int i = 0; i < cloneIndices.length; i++) {
            int firstIndex = cloneIndices[i];
            int secondIndex = eSuff.getLCPMatchIndex(cloneIndices[i]);

            int cloneSize = eSuff.getLCPValue(firstIndex) - 1;

            TokenSourcePair first = getTokenSourcePairFromIndex(firstIndex, cloneSize);
            TokenSourcePair second = getTokenSourcePairFromIndex(secondIndex, cloneSize);

            Range firstRange = converter.convertFromRight(first.getRangeBetween());
            Range secondRange = converter.convertFromRight(second.getRangeBetween());

            CodeClone firstClone = cloneMap.getOrDefault(firstIndex,
                    new CodeClone(first.getUri(), firstRange));
            CodeClone secondClone = cloneMap.getOrDefault(secondIndex,
                    new CodeClone(second.getUri(), secondRange));
            if (cloneSize > firstClone.getCloneSize()) {
                LOGGER.info("Overriden secondClone range");
                firstClone.setRange(firstRange);
                firstClone.setCloneSize(cloneSize);
            }
            if (cloneSize > secondClone.getCloneSize()) {
                LOGGER.info("Overriden firstClone range");
                secondClone.setRange(secondRange);
                secondClone.setCloneSize(cloneSize);
            }

            CodeClone.addMatch(firstClone, secondClone);

            for (CodeClone clone : firstClone.getMatches()) {
                if (clone != firstClone && clone != secondClone) {
                    CodeClone.addMatch(secondClone, clone);
                }
            }

            for (CodeClone clone : secondClone.getMatches()) {
                if (clone != firstClone && clone != secondClone) {
                    CodeClone.addMatch(firstClone, clone);
                }
            }

            cloneMap.put(firstIndex, firstClone);
            cloneMap.put(secondIndex, secondClone);
        }
        return cloneMap;
    }

    private int[] extractCloneIndicesFromSA() {
        Configuration config = Configuration.getInstance();
        int cloneThreshold = config.getCloneTokenThreshold();
        // Fetch clones, ignore contained clones
        ArrayList<Integer> clones = new ArrayList<>();
        for (int i = 0; i < eSuff.size(); i++) {
            if (eSuff.getRank(i) == 0 || eSuff.getLCPMatchIndex(i) == 0) {
                continue;
            }
            int secondIndex = eSuff.getLCPMatchIndex(i);
            // If we find a large enough clone where the match is not contained within
            // another clone, we found a new clone
            if (eSuff.getLCPValue(i) >= cloneThreshold
                    && eSuff.getPreceedingSuffixLCPValue(secondIndex) <= eSuff.getLCPValue(secondIndex)) {
                clones.add(i);
                i++;
                // Ignore all contained clones for the new clones
                while (i < eSuff.size() &&
                        eSuff.getPreceedingSuffixLCPValue(i) > eSuff.getLCPValue(i) &&
                        eSuff.getLCPValue(i) >= cloneThreshold) {
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

    private void buildFingerprints(DocumentIndex<TreesitterDocumentModel> index) {
        Configuration config = Configuration.getInstance();
        Timer timer = new Timer();
        timer.start();
        NotificationHandler.startNotification("index", "Indexing documents");
        int documentsProcessed = 0;
        for (TreesitterDocumentModel document : index) {
            if (!document.hasChanged()) {
                documentsProcessed++;
                continue;
            }

            // Report progress
            NotificationHandler.progressReportNotification("index", documentsProcessed, index.size());

            // If AST is not in memory, build it
            if (!document.hasTree()) {
                document.buildTree();
            }
            document.resetFingerprint();

            // Build fingerprint for document
            Node root = document.getAST().getTree().getRootNode();
            String query = config.getFragmentQuery();

            TSQueryCursor methodsQueryCursor = TreeSitterLibrary.queryPattern(root,
                    query);

            if (methodsQueryCursor == null) {
                LOGGER.info("Invalid pattern");
                return;
            }

            for (TSQueryMatch match = methodsQueryCursor.nextMatch(); match != null; match = methodsQueryCursor
                    .nextMatch()) {
                Node matchNode = match.getCaptures()[0].getNode();

                if (filterRecursiveChildren(matchNode)) {
                    continue;
                }

                Fingerprint fingerprint = fingerprintGenerator.getFingerprint(document.getText(),
                        document.getUri(), matchNode);
                document.addFingerprint(fingerprint);

            }
            // Document has been validated, it is no longer invalid
            document.setChanged(false);

            // Free document resources if it is not open
            if (!document.isOpen()) {
                document.freeText();
                document.freeTree();
            }
            documentsProcessed++;
        }
        NotificationHandler.endNotification("index", null);

        int counter = 0;
        for (TreesitterDocumentModel doc : index) {
            if (doc.hasText()) {
                counter++;
            }
        }
        LOGGER.info("Document files in memory: " + counter);

        timer.stop();
        timer.log("Time to fetch tokens");

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
