// Taken from https://github.com/jakobkhansen/java-algdat/ (my code)
package CCDetect.lsp.datastructures.editdistance;

import java.util.ArrayList;
import java.util.List;

public class EditOperationsCalculator {

    public static List<EditOperation> findEditOperations(int[] s1, int[] s2) {
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

        return getOperations(s1, s2, matrix);

    }

    public static List<EditOperation> getOperations(int[] s1, int[] s2, int[][] matrix) {
        int x = matrix.length - 1;
        int y = matrix[x].length - 1;

        List<EditOperation> operations = new ArrayList<>();

        EditOperation currentOperation = null;
        EditOperationType currentOperationType = EditOperationType.NONE;
        int lastOperationIndex = -1;

        while (x > 0 && y > 0) {
            int subValue = matrix[x - 1][y - 1];
            int delValue = matrix[x - 1][y];
            int insertValue = matrix[x][y - 1];

            if (subValue <= delValue && subValue <= insertValue) {
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
            } else if (insertValue <= delValue && insertValue <= subValue) {

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
            } else {

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

        }
        return operations;
    }

}
