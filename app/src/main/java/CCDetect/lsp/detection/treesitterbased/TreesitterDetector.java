package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
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
    TreesitterFingerprint fingerprint = new TreesitterFingerprint();

    @Override
    public List<CodeClone> getClones() {
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        Configuration config = Configuration.getInstance();
        LOGGER.info("hello world");
        Timer timer = new Timer();
        timer.start();
        for (TreesitterDocumentModel document : index) {

            LOGGER.info("File: " + document.getUri());
            Node root = document.getAST().getTree().getRootNode();
            String query = config.getFragmentQuery();
            LOGGER.info(query);

            TSQueryCursor methodsQueryCursor = TreeSitterLibrary.queryPattern(root,
                    query);

            if (methodsQueryCursor == null) {
                LOGGER.info("Invalid pattern");
                return;
            }

            for (TSQueryMatch match = methodsQueryCursor.nextMatch(); match != null; match = methodsQueryCursor
                    .nextMatch()) {
                Node matchNode = match.getCaptures()[0].getNode();

                LOGGER.info(fingerprint.getFingerprint(matchNode));

            }
        }
        timer.stop();
        timer.log("Time to fetch tokens");
        LOGGER.info("Token count: " + (int) fingerprint.tokenCount);
    }
}
