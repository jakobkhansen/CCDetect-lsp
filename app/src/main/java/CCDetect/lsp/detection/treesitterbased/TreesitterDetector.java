package CCDetect.lsp.detection.treesitterbased;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
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
    public SourceMap sourceMap;
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

        sourceMap = new BinarySearchSourceMap(index);

        // LOGGER.info("Fingerprt : " + Printer.print(getFullFingerprint(index)));

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
                if (!config.isEvaluate()) {
                    eSuff = null;
                }
            }
        } else {

            if (config.isEvaluate()) {
                Timer timer = new Timer();
                timer.start();
                int[] fingerprint = getFullFingerprint(index);
                ExtendedSuffixArray linearEsuff = new SAIS().buildExtendedSuffixArray(fingerprint);
                // LOGGER.info("Expected SA: " + Printer.print(linearEsuff.getSuffix()));
                // LOGGER.info("Expected LCP: " + Printer.print(linearEsuff.getLcp()));
                timer.stop();
                timer.log("Linear time");
            }

            List<EditOperation> edits = getDocumentEdits(index);

            Timer updateTimer = new Timer();
            updateTimer.start();
            dynamicUpdate(edits);
            updateTimer.stop();
            updateTimer.log("Time to update sa");
        }
        for (TreesitterDocumentModel document : index) {
            document.setChanged(false);
        }

        Map<Integer, CodeClone> cloneMap = null;
        if (config.isDynamicDetection()) {
            if (config.isEvaluate()) {
                Timer linearTimer = new Timer();
                linearTimer.start();
                int[] linearCloneIndices = extractCloneIndicesFromSA();
                // LOGGER.info("Linear clone indices: " + Printer.print(linearCloneIndices));

                linearTimer.stop();
                linearTimer.log("Linear time to extract clones");
            }
            Timer extractClonesTimer = new Timer();
            extractClonesTimer.start();
            int[] cloneIndices = extractCloneIndicesFromSAIncremental();

            // LOGGER.info("Clone indices: " + Printer.print(cloneIndices));
            extractClonesTimer.stop();
            cloneMap = getClonesIncremental(cloneIndices);
            extractClonesTimer.log("Time to extract clones");
        } else {
            int[] cloneIndices = extractCloneIndicesFromSA();
            // LOGGER.info("Clone indices: " + Printer.print(cloneIndices));
            cloneMap = getClones(cloneIndices);
        }

        clones = new ArrayList<>();
        for (CodeClone clone : cloneMap.values()) {
            clones.add(clone);
        }
        LOGGER.info("Found " + clones.size() + " clones");

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
        Timer timer = new Timer();
        timer.start();
        List<EditOperation> edits = new ArrayList<>();
        for (TreesitterDocumentModel document : index) {
            if (document.hasChanged()) {
                for (EditOperation edit : document.getEditOperationsFromOldFingerprint()) {
                    edits.add(edit);
                }
            }
        }
        timer.stop();
        timer.log("Time to get edit operations");
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
        // LOGGER.info("Indices : " + Printer.print(indices));
        // LOGGER.info("Linear SA : " + Printer.print(eSuff.getSuffix()));
        // LOGGER.info("Linear LCP: " + Printer.print(eSuff.getLcp()));
        for (int i = 0; i < eSuff.size(); i++) {
            if (eSuff.getRank(i) == 0) {
                continue;
            }

            // If we find a large enough clone where the match is not contained within
            // another clone, we found a new clone
            if (eSuff.getLCPValue(i) >= cloneThreshold) {
                clones.add(i);
                // LOGGER.info("Linear clone added: " + i);
                // LOGGER.info("secondIndex: " + secondIndex);
                if (config.getExcludeContainedClones()) {
                    // Ignore all contained clones for the new clones
                    while (i + 1 < eSuff.size() &&
                            eSuff.getPreceedingSuffixLCPValue(i + 1) > eSuff.getLCPValue(i + 1)) {
                        i++;
                    }
                }
            }
        }
        return Ints.toArray(clones);

    }

    private int[] extractCloneIndicesFromSAIncremental() {
        ArrayList<Integer> clones = new ArrayList<>();

        int lastIndexAdded = -1;
        int lastIndexLCP = -1;
        for (OSTreeNode node : saca.getSA().getNodesAboveThreshold()) {

            int index = OrderStatisticTree.inOrderRank(node.getInverseLink());

            if (lastIndexAdded != -1) {
                int indexDifference = (index - lastIndexAdded);
                if ((node.key + indexDifference) <= lastIndexLCP) {
                    continue;
                }
            }

            clones.add(index);
            lastIndexAdded = index;
            lastIndexLCP = node.key;
        }
        return Ints.toArray(clones);

    }

    private Map<Integer, CodeClone> getClones(int[] cloneIndices) {
        RangeConverter converter = new RangeConverter();
        Map<Integer, CodeClone> cloneMap = new HashMap<>();
        LOGGER.info("Getting linear clones");
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
                firstClone.setRange(firstRange);
                firstClone.setCloneSize(cloneSize);
            }
            if (cloneSize > secondClone.getCloneSize()) {
                secondClone.setRange(secondRange);
                secondClone.setCloneSize(cloneSize);
            }

            unionCloneClass(firstClone, secondClone);

            cloneMap.put(firstIndex, firstClone);
            cloneMap.put(secondIndex, secondClone);
        }
        return cloneMap;
    }

    private Map<Integer, CodeClone> getClonesIncremental(int[] cloneIndices) {
        RangeConverter converter = new RangeConverter();
        Map<Integer, CodeClone> cloneMap = new HashMap<>();
        DynamicPermutation sa = saca.getSA();
        LOGGER.info("Getting incremental clones");
        for (int i = 0; i < cloneIndices.length; i++) {
            int firstIndex = cloneIndices[i];
            OSTreeNode firstNode = sa.getInverseNode(firstIndex);
            int cloneSize = firstNode.key - 1;
            int secondIndex = OrderStatisticTree
                    .inOrderRank(OrderStatisticTree.predecessorOf(firstNode).getInverseLink());

            LOGGER.info("firstIndex: " + firstIndex);
            LOGGER.info("secondIndex: " + secondIndex);
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

            unionCloneClass(firstClone, secondClone);

            cloneMap.put(firstIndex, firstClone);
            cloneMap.put(secondIndex, secondClone);
        }
        return cloneMap;
    }

    public void unionCloneClass(CodeClone first, CodeClone second) {
        if (first.getMatches().contains(second)) {
            return;
        }
        CodeClone.addMatch(first, second);

        for (CodeClone match : first.getMatches()) {
            CodeClone.addMatch(second, match);
        }

        for (CodeClone match : second.getMatches()) {
            CodeClone.addMatch(first, match);
        }

        for (CodeClone match1 : first.getMatches()) {
            for (CodeClone match2 : second.getMatches()) {
                CodeClone.addMatch(match1, match2);
            }
        }
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
            if (document.getMarkedDeleted()) {
                document.resetFingerprint();
                document.setChanged(true);
                documentsProcessed++;
                continue;
            }
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
