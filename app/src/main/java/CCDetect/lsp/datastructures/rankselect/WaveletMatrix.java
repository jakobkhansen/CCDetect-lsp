package CCDetect.lsp.datastructures.rankselect;

import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;

public class WaveletMatrix {
    DynamicTreeBitSet[] matrix;
    int inputSize;
    int bitSetSize;
    int numBitsUsed;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public WaveletMatrix(int[] input, int initialSize) {
        this.inputSize = input.length;
        this.bitSetSize = initialSize;

        numBitsUsed = numberOfBitsUsed(input);
        matrix = new DynamicTreeBitSet[numBitsUsed];
        for (int i = 0; i < numBitsUsed; i++) {
            matrix[i] = new DynamicTreeBitSet(input.length);
        }

        fillMatrix(input, 0);
    }

    public void insertNewRow() {
        LOGGER.info("Inserting new row in wavelet matrix");
        int oldSize = matrix.length;
        DynamicTreeBitSet[] newMatrix = new DynamicTreeBitSet[oldSize + 1];
        newMatrix[0] = new DynamicTreeBitSet(inputSize);
        for (int i = 1; i < newMatrix.length; i++) {
            newMatrix[i] = matrix[i - 1];
        }
        matrix = newMatrix;
        this.numBitsUsed += 1;
    }

    // TODO make this iterative for performance
    public void fillMatrix(int[] input, int level) {
        if (level >= numBitsUsed) {
            return;
        }

        int currentBit = (numBitsUsed - 1) - level;
        for (int i = 0; i < input.length; i++) {
            matrix[level].set(i, getBitBool(input[i], currentBit));
        }

        input = sortByBits(input, currentBit);

        fillMatrix(input, level + 1);
    }

    public DynamicTreeBitSet[] getMatrix() {
        return matrix;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getBitSetSize() {
        return bitSetSize;
    }

    boolean getBitBool(int number, int n) {
        return ((number >> n) & 1) == 1 ? true : false;
    }

    int getBitInt(int number, int n) {
        return ((number >> n) & 1);
    }

    private int numberOfBitsUsed(int[] input) {
        int maxBits = 32 - Integer.numberOfLeadingZeros(input[0]);
        for (int i = 0; i < input.length; i++) {
            int numBits = 32 - Integer.numberOfLeadingZeros(input[i]);
            if (numBits > maxBits) {
            }
            maxBits = maxBits < numBits ? numBits : maxBits;

        }
        return maxBits;
    }

    private int[] sortByBits(int[] input, int bitLocation) {
        int[] out = new int[input.length];
        int[] pointers = new int[2];
        for (int i = 0; i < input.length; i++) {
            if (getBitInt(input[i], bitLocation) == 0) {
                pointers[1]++;
            }
        }
        for (int i = 0; i < input.length; i++) {
            out[pointers[getBitInt(input[i], bitLocation)]] = input[i];
            pointers[getBitInt(input[i], bitLocation)]++;
        }
        return out;
    }

    public int get(int index) {
        int level = 0;
        int current = index;
        int[] bitPositions = new int[matrix.length];
        while (level < matrix.length) {
            bitPositions[level] = current;
            int pos = matrix[level].rank(current, matrix[level].get(current));
            if (matrix[level].get(current)) {
                pos += matrix[level].getNumZeroes();
            }
            current = pos;
            level++;
        }
        int intValue = 0;
        for (int i = 0; i < bitPositions.length; i++) {
            if (matrix[i].get(bitPositions[i])) {
                intValue |= (1 << bitPositions.length - i - 1);
            }
        }
        return intValue;
    }

    public int rank(int index) {
        int level = 0;
        int i = index;
        int p = 0;
        while (level < matrix.length) {
            boolean curr_val = matrix[level].get(i);

            p = matrix[level].rank(p, curr_val);
            i = matrix[level].rank(i, curr_val);
            if (curr_val) {
                p += matrix[level].getNumZeroes();
                i += matrix[level].getNumZeroes();
            }
            level++;
        }
        return i - p;
    }

    public int select(int index, int value) {
        return select(index, value, 0, 0);
    }

    private int select(int index, int value, int level, int p) {
        if (level >= matrix.length) {
            return p + index;
        }
        int currentBit = (numBitsUsed - 1) - level;
        boolean currBit = getBitBool(value, currentBit);
        if (!currBit) {
            p = matrix[level].rank(p, false);
            int j = select(index, value, level + 1, p);
            return matrix[level].select(j, false);
        }
        int numZeroes = matrix[level].getNumZeroes();
        p = numZeroes + matrix[level].rank(p, true);
        int j = select(index, value, level + 1, p);
        return matrix[level].select(j - numZeroes, true);
    }

    public void insert(int index, int value) {
        int numBits = 32 - Integer.numberOfLeadingZeros(value);
        while (numBits > numBitsUsed) {
            insertNewRow();
        }

        int level = 0;
        int currIndex = index;

        while (level < matrix.length) {
            int currentBit = numBitsUsed - level - 1;
            matrix[level].insert(currIndex, getBitBool(value, currentBit));
            currIndex = matrix[level].rank(currIndex, getBitBool(value, currentBit));
            if (getBitBool(value, currentBit)) {
                currIndex += matrix[level].getNumZeroes();
            }
            level++;
        }
        inputSize++;
    }

    public void delete(int index) {
        int level = 0;
        int currIndex = index;
        while (level < matrix.length) {
            int nextIndex = matrix[level].rank(currIndex, matrix[level].get(currIndex));
            nextIndex += matrix[level].get(currIndex) ? matrix[level].getNumZeroes() : 0;
            matrix[level].delete(currIndex);
            currIndex = nextIndex;
            level++;
        }
        inputSize--;
    }

    public int[] toInputArray() {
        int[] out = new int[inputSize];
        for (int i = 0; i < out.length; i++) {
            out[i] = get(i);
        }
        return out;
    }
}
