package CCDetect.lsp.datastructures.editdistance;

import java.util.ArrayList;

public class EditOperation {
    private EditOperationType operationType;
    private int position;
    private ArrayList<Integer> chars = new ArrayList<>();

    public EditOperation(EditOperationType operationType, int start) {
        this.operationType = operationType;
        this.position = start;
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

    public ArrayList<Integer> getChars() {
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
