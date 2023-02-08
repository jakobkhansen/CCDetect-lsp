package CCDetect.lsp.detection.treesitterbased;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.datastructures.DynamicLCP;
import CCDetect.lsp.datastructures.DynamicPermutation;
import CCDetect.lsp.datastructures.DynamicSACA;
import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.OrderStatisticTree;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.datastructures.OrderStatisticTree.OSTreeNode;
import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.detection.treesitterbased.fingerprint.TreesitterFingerprintGenerator;
import CCDetect.lsp.detection.treesitterbased.sourcemap.BinarySearchSourceMap;
import CCDetect.lsp.detection.treesitterbased.sourcemap.DocumentSourceMap;
import CCDetect.lsp.detection.treesitterbased.sourcemap.SourceMap;
import CCDetect.lsp.detection.treesitterbased.sourcemap.TokenSource;
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
    SourceMap sourceMap;
    ExtendedSuffixArray eSuff;
    int tokenCount = 0;
    Configuration config = Configuration.getInstance();
    DynamicSACA saca;

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

        buildFingerprints(index);

        if (sourceMap == null) {
            sourceMap = new BinarySearchSourceMap(index);
        }

        // Build fingerprint

        // Build suffix, inverse, lcp
        LOGGER.info("Building suffix array");
        NotificationHandler.startNotification("clones", "Finding clones");
        if (saca == null || !config.isDynamicDetection()) {
            Timer timer = new Timer();
            timer.start();
            int[] fingerprint = getFullFingerprint(index);
            eSuff = new SAIS().buildExtendedSuffixArray(fingerprint);
            timer.stop();
            timer.log("Linear time");
            if (config.isDynamicDetection() && saca == null) {
                LOGGER.info("Building dynamic structures");
                saca = new DynamicSACA(fingerprint, eSuff);
                LOGGER.info("Built dynamic structures");
                eSuff = null;
            }
        } else {

            if (config.isEvaluate()) {
                Timer timer = new Timer();
                timer.start();
                int[] fingerprint = getFullFingerprint(index);
                ExtendedSuffixArray linearEsuff = new SAIS().buildExtendedSuffixArray(fingerprint);
                timer.stop();
                timer.log("Linear time");
            }

            List<EditOperation> edits = getDocumentEdits(index);

            if (edits.size() == 0) {
                LOGGER.info("No edits found");
                NotificationHandler.endNotification("clones", clones.size() + " clones found");
                return;
            }

            Timer updateTimer = new Timer();
            updateTimer.start();
            dynamicUpdate(edits);
            updateTimer.stop();
            updateTimer.log("Time to update sa");
        }
        for (TreesitterDocumentModel document : index) {
            document.setChanged(false);
        }

        Timer extractClonesTimer = new Timer();
        extractClonesTimer.start();

        Map<Integer, CodeClone> cloneMap = null;
        if (config.isDynamicDetection()) {
            int[] cloneIndices = extractCloneIndicesFromSAIncremental();

            LOGGER.info("Clone indices: " + Printer.print(cloneIndices));
            cloneMap = getClonesIncremental(cloneIndices);
        } else {
            int[] cloneIndices = extractCloneIndicesFromSA();
            cloneMap = getClones(cloneIndices);
        }

        clones = new ArrayList<>();
        for (CodeClone clone : cloneMap.values()) {
            clones.add(clone);
        }
        extractClonesTimer.stop();
        extractClonesTimer.log("Time to extract clones");

        timerIndexChange.stop();
        timerIndexChange.log("indexDidChange time");
        NotificationHandler.endNotification("clones", clones.size() + " clones found");
    }

    public int[] getFullFingerprint(DocumentIndex<TreesitterDocumentModel> index) {
        Timer timer = new Timer();
        timer.start();
        int[] fingerprint = new int[tokenCount + 1];
        int i = 0;
        for (TreesitterDocumentModel doc : index) {
            for (Fingerprint f : doc.getFingerprint()) {
                int[] fingerprintPart = f.getFingerprint();
                for (int j = 0; j < fingerprintPart.length; j++) {
                    int tokenValue = fingerprintPart[j];
                    fingerprint[i] = tokenValue;
                    i++;
                }
            }
        }
        // 0 terminal
        fingerprint[i] = 0;
        timer.stop();
        timer.log("Time to concat full fingerprint");

        return fingerprint;
    }

    public List<EditOperation> getDocumentEdits(DocumentIndex<TreesitterDocumentModel> index) {
        List<EditOperation> edits = new ArrayList<>();
        for (TreesitterDocumentModel document : index) {
            if (document.hasChanged()) {
                for (EditOperation edit : document.getEditOperationsFromOldFingerprint()) {
                    edits.add(edit);
                }
            }
        }
        return edits;
    }

    public void dynamicUpdate(List<EditOperation> edits) {
        for (EditOperation edit : edits) {
            LOGGER.info(Printer.print(edit));
            switch (edit.getOperationType()) {
                case DELETE:
                    saca.deleteFactor(edit);
                    break;
                case INSERT:
                    saca.insertFactor(edit);
                    break;
                case SUBSTITUTE:
                    saca.deleteFactor(edit);
                    saca.insertFactor(edit);
                    break;
                default:
                    break;
            }
        }
    }

    private int[] extractCloneIndicesFromSA() {
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

    private int[] extractCloneIndicesFromSAIncremental() {
        ArrayList<Integer> clones = new ArrayList<>();

        for (OSTreeNode node : saca.getSA().getNodesAboveThreshold()) {
            int index = OrderStatisticTree.inOrderRank(node.getInverseLink());
            if (index == 0) {
                continue;
            }

            OSTreeNode preceedingSANode = OrderStatisticTree.predecessorOf(node.getInverseLink()).getInverseLink();

            // OSTreeNode secondNode = OrderStatisticTree.predecessorOf(node.saLink);
            // int secondIndex = sa.get(sa.getInverse(i) - 1);
            OSTreeNode secondNode = OrderStatisticTree.predecessorOf(node);
            OSTreeNode secondNodePredecessor = OrderStatisticTree.predecessorOf(secondNode.getInverseLink());

            if (secondNodePredecessor == null) {
                continue;
            }

            OSTreeNode secondPreceedingSANode = secondNodePredecessor
                    .getInverseLink();

            int firstLCP = node.key;
            int secondLCP = secondNode.key;
            int firstPreceedingLCP = preceedingSANode.key;
            int secondPreceedingLCP = secondPreceedingSANode.key;

            if (firstPreceedingLCP <= firstLCP && secondPreceedingLCP <= secondLCP) {
                clones.add(index);
            }
        }
        return Ints.toArray(clones);

    }

    private Map<Integer, CodeClone> getClones(int[] cloneIndices) {
        RangeConverter converter = new RangeConverter();
        Map<Integer, CodeClone> cloneMap = new HashMap<>();
        for (int i = 0; i < cloneIndices.length; i++) {
            int firstIndex = cloneIndices[i];
            int secondIndex = eSuff.getLCPMatchIndex(cloneIndices[i]);
            LOGGER.info("firstIndex: " + firstIndex + "\n secondindex: " + secondIndex);

            int cloneSize = eSuff.getLCPValue(firstIndex) - 1;
            LOGGER.info("clone size: " + cloneSize);

            TokenSourcePair first = getTokenSourcePairFromIndex(firstIndex, cloneSize);
            TokenSourcePair second = getTokenSourcePairFromIndex(secondIndex, cloneSize);

            Range firstRange = converter.convertFromRight(first.getRangeBetween());
            Range secondRange = converter.convertFromRight(second.getRangeBetween());

            CodeClone firstClone = cloneMap.getOrDefault(firstIndex,
                    new CodeClone(first.getUri(), firstRange));
            CodeClone secondClone = cloneMap.getOrDefault(secondIndex,
                    new CodeClone(second.getUri(), secondRange));

            if (cloneSize > firstClone.getCloneSize()) {
                firstClone.setRange(firstRange);
                firstClone.setCloneSize(cloneSize);
            }
            if (cloneSize > secondClone.getCloneSize()) {
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

    private Map<Integer, CodeClone> getClonesIncremental(int[] cloneIndices) {
        RangeConverter converter = new RangeConverter();
        Map<Integer, CodeClone> cloneMap = new HashMap<>();
        DynamicPermutation sa = saca.getSA();
        for (int i = 0; i < cloneIndices.length; i++) {
            int firstIndex = cloneIndices[i];
            int secondIndex = sa.get(sa.getInverse(firstIndex) - 1);

            int cloneSize = sa.getLCPValue(sa.getInverse(firstIndex)) - 1;

            TokenSourcePair first = getTokenSourcePairFromIndex(firstIndex, cloneSize);
            TokenSourcePair second = getTokenSourcePairFromIndex(secondIndex, cloneSize);

            Range firstRange = converter.convertFromRight(first.getRangeBetween());
            Range secondRange = converter.convertFromRight(second.getRangeBetween());

            CodeClone firstClone = cloneMap.getOrDefault(firstIndex,
                    new CodeClone(first.getUri(), firstRange));
            CodeClone secondClone = cloneMap.getOrDefault(secondIndex,
                    new CodeClone(second.getUri(), secondRange));

            if (cloneSize > firstClone.getCloneSize()) {
                firstClone.setRange(firstRange);
                firstClone.setCloneSize(cloneSize);
            }
            if (cloneSize > secondClone.getCloneSize()) {
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

    private TokenSourcePair getTokenSourcePairFromIndex(int index, int cloneSize) {

        TokenSource left = sourceMap.getSource(index);
        TokenSource right = sourceMap.getSource(index + cloneSize);

        return new TokenSourcePair(left, right);

    }

    private void buildFingerprints(DocumentIndex<TreesitterDocumentModel> index) {
        Timer timer = new Timer();
        timer.start();
        NotificationHandler.startNotification("index", "Indexing documents");
        int documentsProcessed = 0;
        tokenCount = 0;
        for (TreesitterDocumentModel document : index) {
            if (!document.hasChanged()) {
                documentsProcessed++;
                document.setFingerprintRange(tokenCount, tokenCount + document.getTokenCount() - 1);
                tokenCount += document.getTokenCount();
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

            // Free document resources if it is not open
            if (!document.isOpen()) {
                document.freeText();
                document.freeTree();
            }

            document.setFingerprintRange(tokenCount, tokenCount + document.getTokenCount() - 1);
            documentsProcessed++;
            tokenCount += document.getTokenCount();
        }
        NotificationHandler.endNotification("index", null);

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
