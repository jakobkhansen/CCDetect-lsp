package CCDetect.lsp.files.TreesitterIndex;

import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.files.DocumentModel;
import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.TSPoint;

public class TreesitterDocumentModel extends DocumentModel {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private TreesitterDocumentAST ast;
    private String text;

    public TreesitterDocumentModel(String uri, String text) {
        super(uri, text);
        this.text = text;
    }

    public void buildTree() {
        ast = new TreesitterDocumentAST(text);
    }

    public void freeTree() {
        if (ast != null) {
            ast.free();
        }
        ast = null;
    }

    public TreesitterDocumentAST getAST() {
        return ast;
    }

    // TODO refactor this and ensure correct
    public void updateDocument(Range range, String updatedContent) {
        Position startPos = range.getStart();
        Position endPos = range.getEnd();

        int startLine = startPos.getLine();
        int endLine = endPos.getLine();

        int startChar = startPos.getCharacter();
        int endChar = endPos.getCharacter();

        int start = 0, end = 0;
        int currLine = 0, currChar = 0;

        for (int i = 0; i < text.length(); i++) {

            char c = text.charAt(i);

            if (currLine == startLine && currChar == startChar) {
                start = i;
            }
            if (currLine == endLine && currChar == endChar) {
                end = i;
                break;
            }
            currChar++;
            if (c == '\n') {
                currLine++;
                currChar = 0;
            }
        }
        String prefix = text.substring(0, start);
        String suffix = text.substring(end, text.length());
        text = prefix + updatedContent + suffix;

        if (ast == null) {
            buildTree();
            return;
        }

        int numLinesInEdit = text.split("\n").length;
        int numCharsInLastLineOfEdit = text.split("\n")[text.split("\n").length - 1].length();

        TSPoint startPoint = new TSPoint(startLine, startChar);
        TSPoint oldEndPoint = new TSPoint(endLine, endChar);
        TSPoint newEndPoint = new TSPoint(startLine + numLinesInEdit, numCharsInLastLineOfEdit);

        TSInputEdit edit = new TSInputEdit(start, end, start + updatedContent.length(), startPoint, oldEndPoint,
                newEndPoint);
        ast.incrementalUpdate(text, edit);
    }
}
