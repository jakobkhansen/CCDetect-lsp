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

/**
 * TreesitterDetector
 */
public class TreesitterDetector implements CloneDetector<TreesitterDocumentModel> {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);
    List<CodeClone> clones = new ArrayList<>();
    TreesitterFingerprintGenerator fingerprintGenerator = new TreesitterFingerprintGenerator();

    @Override
    public List<CodeClone> getClones() {
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        FingerprintIndex fingerprintIndex = buildFingerprintIndex(index);
        LOGGER.info("Token count: " + (int) fingerprintGenerator.tokenCount);
        LOGGER.info(Printer.print(fingerprintGenerator));
        // Testing
        ArrayList<Integer> fullFingerprint = new ArrayList<>();
        for (Fingerprint f : fingerprintIndex.fingerprints) {
            for (int i : f.getFingerprint()) {
                fullFingerprint.add(i);
            }
        }
        // 0 terminal
        fullFingerprint.add(0);
        LOGGER.info("fullFingerprint size: " + fullFingerprint.size());
        int[] fingerprint = Ints.toArray(fullFingerprint);

        ExtendedSuffixArray suff = new SAIS().buildExtendedSuffixArray(fingerprint);
        LOGGER.info("Suffix: " + Printer.print(suff.getSuffix()));
        LOGGER.info("LCP: " + Printer.print(suff.getLcp()));

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

                int[] fingerprintString = fingerprintGenerator.getFingerprint(document.getText(), matchNode);
                Fingerprint fingerprint = new Fingerprint(fingerprintString, document.getUri(), matchNode.toRange());

                fingerprintIndex.add(fingerprint);
            }
            document.freeTree();
        }
        timer.stop();
        timer.log("Time to fetch tokens");

        return fingerprintIndex;
    }
}
