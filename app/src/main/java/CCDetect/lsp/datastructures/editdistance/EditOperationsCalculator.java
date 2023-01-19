// Taken from https://github.com/jakobkhansen/java-algdat/ (my code)
package CCDetect.lsp.datastructures.editdistance;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;

public class EditOperationsCalculator {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public static int getEqualCharactersStart(int[] s1, int[] s2) {
        for (int i = 0; i < Math.min(s1.length, s2.length); i++) {
            if (i >= s1.length || i >= s2.length || s1[i] != s2[i]) {
                return i;
            }
        }
        return s1.length;
    }

    public static int getEqualCharactersEnd(int[] s1, int[] s2, int startOffset) {
        int s1Index = s1.length - 1;
        int s2Index = s2.length - 1;

        while (s1Index >= startOffset && s2Index >= startOffset && s1[s1Index] == s2[s2Index]) {
            s1Index--;
            s2Index--;
        }
        return s1.length - s1Index - 1;
    }

    public static List<EditOperation> findEditOperations(int[] s1, int[] s2) {
        int startOffset = getEqualCharactersStart(s1, s2);
        int endOffset = getEqualCharactersEnd(s1, s2, startOffset);
        int commonElements = (startOffset + endOffset);
        int[] newS1 = new int[s1.length - commonElements];
        int[] newS2 = new int[s2.length - commonElements];
        for (int i = 0; i < newS1.length; i++) {
            newS1[i] = s1[i + startOffset];
        }
        for (int i = 0; i < newS2.length; i++) {
            newS2[i] = s2[i + startOffset];
        }
        s1 = newS1;
        s2 = newS2;
        int[][] matrix = new int[s1.length + 1][s2.length + 1];

        for (int i = 0; i < matrix.length; i++) {
            matrix[i][0] = i;
        }

        for (int j = 0; j < matrix[0].length; j++) {
            matrix[0][j] = j;
        }

        for (int i = 1; i < matrix.length; i++) {
            for (int j = 1; j < matrix[i].length; j++) {
                if (s1[i - 1] == s2[j - 1]) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = Math.min(matrix[i - 1][j - 1], Math.min(matrix[i - 1][j], matrix[i][j - 1])) + 1;
                }
            }
        }

        LOGGER.info("Getting operations");
        return getOperations(s1, s2, matrix, startOffset);

    }

    public static List<EditOperation> getOperations(int[] s1, int[] s2, int[][] matrix, int startOffset) {
        int x = matrix.length - 1;
        int y = matrix[x].length - 1;

        List<EditOperation> operations = new ArrayList<>();

        EditOperation currentOperation = null;
        EditOperationType currentOperationType = EditOperationType.NONE;
        int lastOperationIndex = -1;

        while (x > 0 || y > 0) {
            int subValue = x != 0 && y != 0 ? matrix[x - 1][y - 1] : Integer.MAX_VALUE;
            int delValue = x != 0 ? matrix[x - 1][y] : Integer.MAX_VALUE;
            int insertValue = y != 0 ? matrix[x][y - 1] : Integer.MAX_VALUE;
            if (insertValue <= delValue && insertValue <= subValue) {

                // Continue last operation or create new
                if (currentOperationType == EditOperationType.INSERT && lastOperationIndex == x) {
                    currentOperation.getChars().add(0, s2[y - 1]);
                } else {
                    currentOperation = new EditOperation(EditOperationType.INSERT, x, x);
                    currentOperation.getChars().add(0, s2[y - 1]);
                    currentOperationType = EditOperationType.INSERT;
                    lastOperationIndex = x;
                    operations.add(0, currentOperation);
                }
                y--;
            } else if (delValue <= subValue && delValue <= insertValue) {

                // Continue last operation or create new
                if (currentOperationType == EditOperationType.DELETE && lastOperationIndex == x) {
                    currentOperation.getChars().add(0, s1[x - 1]);
                    currentOperation.setPosition(currentOperation.getPosition() - 1);
                } else {
                    currentOperation = new EditOperation(EditOperationType.DELETE, x - 1, x - 1);
                    currentOperation.getChars().add(0, s1[x - 1]);
                    operations.add(0, currentOperation);
                    currentOperationType = EditOperationType.DELETE;
                }
                lastOperationIndex = x - 1;
                x--;
            }

            else {
                if (subValue != matrix[x][y]) {
                    // Continue last operation or create new
                    if (currentOperationType == EditOperationType.SUBSTITUTE && lastOperationIndex == x) {
                        currentOperation.getChars().add(0, s2[y - 1]);
                        currentOperation.decrementPosition();
                    } else {
                        currentOperation = new EditOperation(EditOperationType.SUBSTITUTE, x - 1, x - 1);
                        currentOperation.getChars().add(0, s2[y - 1]);
                        currentOperationType = EditOperationType.SUBSTITUTE;
                        operations.add(0, currentOperation);
                    }
                    lastOperationIndex = x - 1;
                }
                x--;
                y--;
            }
        }
        for (EditOperation edit : operations) {
            edit.setPosition(edit.getPosition() + startOffset);
        }
        return operations;
    }

}
