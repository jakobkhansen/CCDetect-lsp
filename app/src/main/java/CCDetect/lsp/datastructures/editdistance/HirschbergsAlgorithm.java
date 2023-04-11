package CCDetect.lsp.datastructures.editdistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;

/**
 * Hirschbergs
 */
public class HirschbergsAlgorithm {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    int[] s1, s2;
    int startOffset;
    int endOffset;

    class Pair {
        public int x, y;

        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }
    }

    public HirschbergsAlgorithm(int[] s1, int[] s2) {
        this.s1 = s1;
        this.s2 = s2;
        startOffset = getEqualCharactersStart(s1, s2);
        endOffset = getEqualCharactersEnd(s1, s2, startOffset);
    }

    public boolean cacheContains(int[][] cache, int i, int j) {
        return i >= 0 && i < cache.length && j >= 0 && j < cache[0].length;
    }

    public static int getEqualCharactersStart(int[] s1, int[] s2) {
        if (s1.length == 0 || s2.length == 0) {
            return 0;
        }
        for (int i = 0; i < Math.min(s1.length, s2.length); i++) {
            if (s1[i] != s2[i]) {
                return i;
            }
        }
        return Math.min(s1.length, s2.length);
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

    public List<Pair> getEditPositions() {
        int commonElements = (startOffset + endOffset);
        int[] newS1 = new int[s1.length - commonElements];
        int[] newS2 = new int[s2.length - commonElements];
        for (int i = 0; i < newS1.length; i++) {
            newS1[i] = s1[i + startOffset];
        }
        for (int i = 0; i < newS2.length; i++) {
            newS2[i] = s2[i + startOffset];
        }

        this.s1 = newS1;
        this.s2 = newS2;

        List<Pair> positions = new ArrayList<>();

        if (s1.length == 0 && s2.length == 0) {
            return positions;
        }

        positions.add(new Pair(0, 0));
        positions.addAll(hirschbergs_rec(0, s1.length, 0, s2.length));
        positions.add(new Pair(s1.length, s2.length));

        return positions;
    }

    private List<Pair> hirschbergs_rec(int row_start, int row_end, int column_start, int column_end) {

        // I think for these we need to add every node from where we are back to 0,0
        int rowLength = (row_end - row_start);
        int columnLength = (column_end - column_start);
        if (rowLength == 0) {
            List<Pair> pairs = new ArrayList<>();
            for (int i = columnLength - 1; i > 0; i--) {
                Pair newPair = new Pair(row_start, i + column_start);
                pairs.add(0, newPair);
            }
            return pairs;
        } else if (columnLength == 0) {
            List<Pair> pairs = new ArrayList<>();
            for (int i = rowLength - 1; i > 0; i--) {
                Pair newPair = new Pair(row_start + i, column_start);
                pairs.add(0, newPair);
            }
            return pairs;
        } else if (rowLength == 1 || columnLength == 1) {
            return wagner_fischer_small(row_start, row_end, column_start, column_end);
        }
        int mid = (row_start + row_end) / 2;
        int[] scoreL = needlemanwunsch_score(row_start, mid + 1, column_start, column_end);
        int[] scoreR = needlemanwunsch_score_reverse(mid, row_end, column_start, column_end);

        int lowest = Integer.MAX_VALUE;
        int lowest_index = -1;
        for (int i = 0; i < scoreL.length; i++) {
            int score = scoreL[i] + scoreR[i];
            if (score < lowest) {
                lowest = score;
                lowest_index = i;
            }
        }
        lowest_index = column_start + lowest_index;
        Pair newPair = new Pair(mid, lowest_index);
        List<Pair> result = new ArrayList<>();
        List<Pair> left = hirschbergs_rec(row_start, mid, column_start, lowest_index);
        result.addAll(left);

        result.add(newPair);

        List<Pair> right = hirschbergs_rec(mid, row_end, lowest_index, column_end);
        result.addAll(right);

        return result;
    }

    public List<Pair> wagner_fischer_small(int row_start, int row_end, int column_start, int column_end) {
        int num_rows = row_end - row_start + 1;
        int num_cols = column_end - column_start + 1;
        int[][] cache = new int[num_rows][num_cols];
        for (int i = 0; i < num_rows; i++) {
            cache[i][0] = i;
        }
        for (int i = 0; i < num_cols; i++) {
            cache[0][i] = i;
        }
        // 5 6 3 6
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_cols; j++) {
            }
        }
        for (int i = row_start + 1; i <= row_end; i++) {
            for (int j = column_start + 1; j <= column_end; j++) {
                int cache_row = i - row_start;
                int cache_col = j - column_start;

                if (s1[i - 1] == s2[j - 1]) {
                    cache[cache_row][cache_col] = cache[cache_row - 1][cache_col - 1];
                } else {
                    int minVal = Math.min(cache[cache_row - 1][cache_col - 1],
                            Math.min(cache[cache_row - 1][cache_col], cache[cache_row][cache_col - 1]));
                    cache[cache_row][cache_col] = minVal + 1;
                }
            }
        }

        List<Pair> pairs = new ArrayList<>();
        Pair current = new Pair(row_end, column_end);

        while (current.x > row_start || current.y > column_start) {
            int x = current.x;
            int y = current.y;
            int cacheX = x - row_start;
            int cacheY = y - column_start;

            int subVal = cacheContains(cache, cacheX - 1, cacheY - 1) ? cache[cacheX - 1][cacheY - 1]
                    : Integer.MAX_VALUE;
            int insertVal = cacheContains(cache, cacheX, cacheY - 1) ? cache[cacheX][cacheY - 1] : Integer.MAX_VALUE;
            int delVal = cacheContains(cache, cacheX - 1, cacheY) ? cache[cacheX - 1][cacheY] : Integer.MAX_VALUE;

            Pair nextPair = null;
            // if (subVal <= insertVal && subVal <= delVal) {
            // nextPair = new Pair(x - 1, y - 1);
            // } else if (insertVal <= subVal && insertVal <= delVal) {
            // nextPair = new Pair(x, y - 1);
            // } else {
            // nextPair = new Pair(x - 1, y);
            // }

            if (insertVal <= subVal && delVal <= subVal) {
                nextPair = new Pair(x, y - 1);
            } else if (delVal <= insertVal && delVal <= subVal) {
                nextPair = new Pair(x - 1, y);
            } else {
                nextPair = new Pair(x - 1, y - 1);
            }
            current = nextPair;
            if (current.x > row_start || current.y > column_start) {

                pairs.add(0, current);
            }
        }
        return pairs;

    }

    public int[] needlemanwunsch_score(int row_start, int row_end, int column_start, int column_end) {
        int row_length = column_end - column_start + 1;
        int[][] cache = new int[2][row_length];
        for (int i = column_start; i <= column_end; i++) {
            cache[0][i - column_start] = i - column_start;
        }
        for (int i = row_start + 1; i < row_end; i++) {
            cache[1][0] = cache[0][0] + 1;
            for (int j = column_start + 1; j <= column_end; j++) {
                int cache_col = j - column_start;

                int subVal = cache[0][cache_col - 1];
                subVal -= s1[i - 1] == s2[cache_col - 1] ? 1 : 0;

                int delVal = cache[0][cache_col];
                int insertVal = cache[1][cache_col - 1];

                cache[1][cache_col] = Math.min(subVal, Math.min(delVal, insertVal)) + 1;
            }
            for (int k = 0; k < row_length; k++) {
                cache[0][k] = cache[1][k];
            }
        }
        return cache[1];
    }

    public int[] needlemanwunsch_score_reverse(int row_start, int row_end, int column_start, int column_end) {
        int row_length = column_end - column_start + 1;
        int[][] cache = new int[2][row_length];
        for (int i = column_end; i >= column_start; i--) {
            cache[0][i - column_start] = column_end - i;
        }
        for (int i = row_end - 1; i >= row_start; i--) {
            cache[1][0] = cache[0][0] + 1;
            for (int j = column_end - 1; j >= column_start; j--) {
                int cache_col = j - column_start;

                int subVal = cache[0][cache_col + 1];
                subVal -= s1[i] == s2[j] ? 1 : 0;

                int delVal = cache[0][cache_col];
                int insertVal = cache[0][cache_col + 1];

                cache[1][cache_col] = Math.min(subVal, Math.min(delVal, insertVal)) + 1;
            }
            for (int k = 0; k < row_length; k++) {
                cache[0][k] = cache[1][k];
            }
        }
        return cache[1];
    }

    public List<EditOperation> getOperations() {
        List<Pair> positions = getEditPositions();

        List<EditOperation> operations = new ArrayList<>();
        if (positions.size() < 2) {
            return operations;
        }

        EditOperation currentOperation = null;
        EditOperationType currentOperationType = EditOperationType.NONE;
        int lastOperationIndex = -1;

        for (int i = positions.size() - 1; i > 0; i--) {
            Pair currentPair = positions.get(i);
            int x = currentPair.x;
            int y = currentPair.y;
            Pair nextPair = positions.get(i - 1);
            int nextX = nextPair.x;
            int nextY = nextPair.y;

            if (x - 1 == nextX && y - 1 == nextY) {
                if (s1[x - 1] != s2[y - 1]) {
                    // Continue last operation or create new
                    if (currentOperationType == EditOperationType.SUBSTITUTE && lastOperationIndex == x) {
                        currentOperation.getChars().add(0, s2[y - 1]);
                        lastOperationIndex = x - 1;
                        currentOperation.decrementPosition();
                    } else {
                        currentOperation = new EditOperation(EditOperationType.SUBSTITUTE, x - 1);
                        currentOperation.getChars().add(0, s2[y - 1]);
                        currentOperationType = EditOperationType.SUBSTITUTE;
                        lastOperationIndex = x - 1;
                        operations.add(currentOperation);
                    }
                }
                x--;
                y--;
            } else if (y - 1 == nextY) {

                // Continue last operation or create new
                if (currentOperationType == EditOperationType.INSERT && lastOperationIndex == x) {
                    currentOperation.getChars().add(0, s2[y - 1]);
                } else {
                    currentOperation = new EditOperation(EditOperationType.INSERT, x);
                    currentOperation.getChars().add(0, s2[y - 1]);
                    currentOperationType = EditOperationType.INSERT;
                    lastOperationIndex = x;
                    operations.add(currentOperation);
                }
                y--;
            } else {
                if (currentOperationType == EditOperationType.DELETE && lastOperationIndex == x) {
                    currentOperation.getChars().add(0, s1[x - 1]);
                    currentOperation.decrementPosition();
                    lastOperationIndex = x - 1;
                } else {
                    currentOperation = new EditOperation(EditOperationType.DELETE, x - 1);
                    currentOperation.getChars().add(0, s1[x - 1]);
                    currentOperationType = EditOperationType.DELETE;
                    lastOperationIndex = x - 1;
                    operations.add(currentOperation);
                }
                x--;
            }
        }
        for (EditOperation operation : operations) {
            operation.setPosition(operation.getPosition() + startOffset);
        }

        return operations;
    }

}
