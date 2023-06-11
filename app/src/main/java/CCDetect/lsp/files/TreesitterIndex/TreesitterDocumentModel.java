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
import CCDetect.lsp.server.Configuration;
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
    private boolean deleted = false;

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

        if (ast == null || !Configuration.getInstance().isIncrementalParsing()) {
            buildTree();
            return;
        }

        int numLinesInEdit = updatedContent.split("\n").length - 1;
        int numCharsInLastLineOfEdit = updatedContent.split("\n")[numLinesInEdit - 1].length();

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

        int startOffset = HirschbergsAlgorithm.getEqualCharactersStart(fullOldFingerprint,
                fullNewFingerprint);
        int endOffset = HirschbergsAlgorithm.getEqualCharactersEnd(fullOldFingerprint,
                fullNewFingerprint, startOffset);

        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(fullOldFingerprint,
                fullNewFingerprint);
        List<EditOperation> operations = hirschbergs.getOperations();

        if (operations.size() >= 10) {
            LOGGER.info("Using insert+delete edit distance optimization, startOffset: " + startOffset + ", endOffset: "
                    + endOffset);
            return getDeleteAndInsertOnly(fullOldFingerprint, fullNewFingerprint,
                    startOffset, endOffset);
        }

        // Update positions of all operations based on the fingerprints offset
        for (EditOperation operation : operations) {
            operation.setPosition(operation.getPosition() + fingerprintStart);
        }

        return operations;
    }

    public List<EditOperation> getDeleteAndInsertOnly(int[] fullOldFingerprint, int[] fullNewFingerprint,
            int startOffset, int endOffset) {

        List<EditOperation> operations = new ArrayList<>();

        EditOperation delete = new EditOperation(EditOperationType.DELETE, fingerprintStart + startOffset);
        EditOperation insert = new EditOperation(EditOperationType.INSERT, fingerprintStart + startOffset);

        for (int i = startOffset; i < fullOldFingerprint.length - endOffset; i++) {
            delete.getChars().add(fullOldFingerprint[i]);
        }

        for (int i = startOffset; i < fullNewFingerprint.length - endOffset; i++) {
            insert.getChars().add(fullNewFingerprint[i]);
        }

        if (delete.getChars().size() > 0) {
            operations.add(delete);
        }

        if (insert.getChars().size() > 0) {
            operations.add(insert);
        }

        return operations;
    }

    public void setMarkedDeleted(boolean value) {
        this.deleted = value;
    }

    public boolean getMarkedDeleted() {
        return this.deleted;
    }

    public EditOperation getDeleteFileOperation() {

        int[] fullFingerprint = getFullFingerprint();
        EditOperation delete = new EditOperation(EditOperationType.DELETE, fingerprintStart);
        for (int i = 0; i < fullFingerprint.length; i++) {
            delete.getChars().add(fullFingerprint[i]);
        }

        return delete;
    }
}
