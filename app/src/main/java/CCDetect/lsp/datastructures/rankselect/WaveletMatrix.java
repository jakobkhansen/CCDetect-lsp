package CCDetect.lsp.datastructures.rankselect;

import CCDetect.lsp.utils.Printer;

public class WaveletMatrix {
    DynamicBitSet[] matrix;
    int inputSize;
    int bitSetSize;
    int numBitsUsed;

    public WaveletMatrix(int[] input, int initialSize) {
        this.inputSize = input.length;
        this.bitSetSize = initialSize;

        numBitsUsed = numberOfBitsUsed(input);
        matrix = new DynamicBitSet[numBitsUsed];
        for (int i = 0; i < numBitsUsed; i++) {
            matrix[i] = new DynamicBitSet(input.length, initialSize);
        }

        fillMatrix(input, 0);
    }

    public void rebuildMatrix(int numBitsUsed, int newSize) {

        int[] oldInput = new int[inputSize];
        for (int i = 0; i < inputSize; i++) {
            oldInput[i] = access(i);
        }
        this.numBitsUsed = numBitsUsed;
        this.bitSetSize = newSize;
        matrix = new DynamicBitSet[numBitsUsed];
        for (int i = 0; i < numBitsUsed; i++) {
            matrix[i] = new DynamicBitSet(oldInput.length, newSize);
        }

        fillMatrix(oldInput, 0);

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

    public DynamicBitSet[] getMatrix() {
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

    public int access(int index) {
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

    public void insert(int index, int value) {
        int level = 0;
        int currIndex = index;
        int numBits = 32 - Integer.numberOfLeadingZeros(value);
        if (numBits > numBitsUsed) {
            rebuildMatrix(numBits, bitSetSize + 100);
        }
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
            out[i] = access(i);
        }
        return out;
    }
}
