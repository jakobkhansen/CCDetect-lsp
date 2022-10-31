package CCDetect.lsp.files.TreesitterIndex;

import java.util.logging.Logger;

import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.Tree;

public class TreesitterDocumentAST {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Parser parser;
    private Tree tree;

    public TreesitterDocumentAST(String documentContent) {
        parser = TreeSitterLibrary.getParser();
        try {
            tree = parser.parseString(documentContent);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
    }

    public void free() {
        if (tree != null) {
            tree.close();
        }
    }

    public void incrementalUpdate(String newDocumentContent, TSInputEdit edit) {
        tree.edit(edit);
        try {
            tree = parser.parseString(tree, newDocumentContent);
        } catch (Exception e) {
            LOGGER.info("Exception while parsing");
        }
    }

    public Tree getTree() {
        return tree;
    }
}
