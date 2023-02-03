package CCDetect.lsp.files.TreesitterIndex;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.editdistance.EditOperationType;
import CCDetect.lsp.datastructures.editdistance.HirschbergsAlgorithm;
import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.utils.Printer;
import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.TSPoint;

public class TreesitterDocumentModel extends DocumentModel {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    private TreesitterDocumentAST ast;
    private ArrayList<Fingerprint> fingerprints = new ArrayList<>();
    private ArrayList<Fingerprint> oldFingerprints = new ArrayList<>();
    private boolean hasChanged = true;

    int tokenCount = 0;

    int fingerprintStart = 0;
    int fingerprintEnd = 0;

    public TreesitterDocumentModel(Path path, String text) {
        super(path, text);
    }

    public void buildTree() {
        ast = new TreesitterDocumentAST(getText());
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

    public boolean hasTree() {
        return ast != null;
    }

    public ArrayList<Fingerprint> getFingerprint() {
        return fingerprints;
    }

    public void setFingerprints(ArrayList<Fingerprint> fingerprints) {
        this.fingerprints = fingerprints;
    }

    public ArrayList<Fingerprint> getOldFingerprints() {
        return oldFingerprints;
    }

    public void setOldFingerprints(ArrayList<Fingerprint> oldFingerprints) {
        this.oldFingerprints = oldFingerprints;
    }

    public void addFingerprint(Fingerprint fingerprint) {
        this.fingerprints.add(fingerprint);
        tokenCount += fingerprint.getFingerprint().length;
    }

    public void resetFingerprint() {
        this.oldFingerprints = this.fingerprints;
        this.fingerprints = new ArrayList<>();
        tokenCount = 0;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setChanged(boolean value) {
        hasChanged = value;
    }

    public void setFingerprintRange(int start, int end) {
        this.fingerprintStart = start;
        this.fingerprintEnd = end;
    }

    public int getFingerprintStart() {
        return fingerprintStart;
    }

    public int getFingerprintEnd() {
        return fingerprintEnd;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public int[] getFullFingerprint() {
        return concatFingerprints(fingerprints);
    }

    public int[] getFullOldFingerprint() {
        return concatFingerprints(oldFingerprints);
    }

    private int[] concatFingerprints(List<Fingerprint> fingerprints) {
        List<Integer> fullFingerprintList = new ArrayList<>();

        for (Fingerprint f : fingerprints) {
            for (int i : f.getFingerprint()) {
                fullFingerprintList.add(i);
            }
        }
        int[] fullFingerprint = fullFingerprintList.stream().mapToInt(i -> i).toArray();

        return fullFingerprint;

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

        for (int i = 0; i < getText().length(); i++) {

            char c = getText().charAt(i);

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

        String prefix = getText().substring(0, start);
        String suffix = getText().substring(end, getText().length());
        setText(prefix + updatedContent + suffix);
        setChanged(true);

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

    public List<EditOperation> getEditOperationsFromOldFingerprint() {
        int[] fullOldFingerprint = getFullOldFingerprint();
        int[] fullNewFingerprint = getFullFingerprint();

        HirschbergsAlgorithm algorithm = new HirschbergsAlgorithm(fullOldFingerprint,
                fullNewFingerprint);
        List<EditOperation> operations = algorithm.getOperations();

        // Update positions of all operations based on the fingerprints offset
        for (EditOperation operation : operations) {
            operation.setPosition(operation.getPosition() + fingerprintStart);
        }

        return operations;
    }
}
