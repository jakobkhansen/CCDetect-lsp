package CCDetect.lsp.files.TreesitterIndex;

import java.util.logging.Logger;

import CCDetect.lsp.treesitter.Treesitter;
import CCDetect.lsp.utils.Printer;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.Tree;

public class TreesitterDocumentAST {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Parser parser;
    private Tree tree;

    public TreesitterDocumentAST(String documentContent) {
        parser = Treesitter.getParser();
        try {
            tree = parser.parseString(documentContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void incrementalUpdate(String newDocumentContent, TSInputEdit edit) {
        LOGGER.info(Printer.print(edit));
        tree.edit(edit);
        try {
            tree = parser.parseString(tree, newDocumentContent);
        } catch (Exception e) {
            LOGGER.info("Exception while parsing");
        }
        LOGGER.info(tree.getRootNode().getNodeString());
    }

    public Tree getTree() {
        return tree;
    }
}
