package CCDetect.lsp.datastructures.editdistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;

public class EditOperation {
    private EditOperationType operationType;
    private int position;
    private List<Integer> chars = new ArrayList<>();

    public EditOperation(EditOperationType operationType, int start) {
        this.operationType = operationType;
        this.position = start;
    }

    public EditOperation(EditOperationType operationType, int start, int[] chars) {
        this.operationType = operationType;
        this.position = start;
        this.chars = Arrays.stream(chars).boxed().collect(Collectors.toList());
    }

    public EditOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(EditOperationType operationType) {
        this.operationType = operationType;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int start) {
        this.position = start;
    }

    public List<Integer> getChars() {
        return chars;
    }

    public void setChars(ArrayList<Integer> chars) {
        this.chars = chars;
    }

    public void incrementPosition() {
        this.position++;
    }

    public void decrementPosition() {
        this.position--;
    }
};
