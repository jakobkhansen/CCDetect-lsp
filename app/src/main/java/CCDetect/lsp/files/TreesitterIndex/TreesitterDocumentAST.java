package CCDetect.lsp.files.TreesitterIndex;

import java.util.logging.Logger;

import CCDetect.lsp.treesitter.Treesitter;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.Tree;

public class TreesitterDocumentAST {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Parser parser;
    private Tree tree;

    public TreesitterDocumentAST(String documentContent) {
        LOGGER.info("Constructor");
        parser = Treesitter.getParser();
        LOGGER.info("Got parser");
        try {
            tree = parser.parseString(documentContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void incrementalUpdate(String newDocumentContent, TSInputEdit edit) {
        tree.edit(edit);
        try {
            parser.parseString(tree, newDocumentContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Tree getTree() {
        return tree;
    }
}
