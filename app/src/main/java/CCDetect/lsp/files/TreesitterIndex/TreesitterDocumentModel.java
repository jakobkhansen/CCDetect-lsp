package CCDetect.lsp.files.TreesitterIndex;

import java.util.logging.Logger;

import CCDetect.lsp.files.DocumentModel;

public class TreesitterDocumentModel extends DocumentModel {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private TreesitterDocumentAST ast;

    public TreesitterDocumentModel(String uri, String text) {
        super(uri, text);
        LOGGER.info("Test");
        ast = new TreesitterDocumentAST(text);
        LOGGER.info("AST for " + uri);
        LOGGER.info(ast.getTree().getRootNode().getNodeString());
    }
}
