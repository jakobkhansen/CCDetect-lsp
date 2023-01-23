package CCDetect.lsp.datastructures.rankselect;

import java.util.BitSet;

import CCDetect.lsp.utils.Printer;

public class WaveletMatrix {
    BitSet[] matrix;
    int[] numZeroes;
    int inputSize;
    int bitSetSize;

    public WaveletMatrix(int[] input, int initialSize) {
        this.inputSize = input.length;
        this.bitSetSize = initialSize;

        int height = numberOfBitsUsed(input);
        matrix = new BitSet[height];
        numZeroes = new int[height];
        for (int i = 0; i < height; i++) {
            matrix[i] = new BitSet(initialSize);
        }

        int numBits = numberOfBitsUsed(input) - 1;
        fillMatrix(input, 0, numBits);
    }

    public void fillMatrix(int[] input, int level, int numBits) {
        if (level > numBits) {
            return;
        }

        int currentBit = numBits - level;
        for (int i = 0; i < input.length; i++) {
            matrix[level].set(i, getBitBool(input[i], currentBit));
        }

        input = sortByBits(input, currentBit);
        int numZeroesInLevel = 0;
        while (!getBitBool(input[numZeroesInLevel], currentBit)) {
            numZeroesInLevel++;
        }
        numZeroes[level] = numZeroesInLevel;

        fillMatrix(input, level + 1, numBits);
    }

    public BitSet[] getMatrix() {
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
            int pos = rank(level, current, matrix[level].get(current));
            if (matrix[level].get(current)) {
                pos += numZeroes[level];
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

    public int rank(int level, int index, boolean bool) {
        int res = 0;
        for (int i = 0; i < index; i++) {
            res += matrix[level].get(i) == bool ? 1 : 0;
        }
        return res;
    }
}
