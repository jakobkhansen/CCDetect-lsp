package CCDetect.lsp.detection.treesitterbased;

import java.util.List;
import java.util.logging.Logger;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSQueryCapture;
import ai.serenade.treesitter.TSQueryCursor;
import ai.serenade.treesitter.TSQueryMatch;

/**
 * TreesitterDetector
 */
public class TreesitterDetector implements CloneDetector<TreesitterDocumentModel> {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    @Override
    public List<CodeClone> getClones() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onIndexChange(DocumentIndex<TreesitterDocumentModel> index) {
        for (TreesitterDocumentModel document : index) {

            LOGGER.info("File: " + document.getUri());
            Node root = document.getAST().getTree().getRootNode();
            String pattern = "(function_definition) @method";

            TSQueryCursor methodsQueryCursor = TreeSitterLibrary.queryPattern(root,
                    pattern);

            if (methodsQueryCursor == null) {
                LOGGER.info("Invalid pattern");
                return;
            }

            LOGGER.info(methodsQueryCursor.nextMatch() + "");

            for (TSQueryMatch match = methodsQueryCursor.nextMatch(); match != null; match = methodsQueryCursor
                    .nextMatch()) {
                LOGGER.info(match.getCaptures()[0].getNode().getNodeString());
            }
            LOGGER.info("test");
        }
    }
}
