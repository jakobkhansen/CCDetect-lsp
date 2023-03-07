package CCDetect.lsp.datastructures;

/**
 * ExtendedSuffixArray
 */
public class ExtendedSuffixArray {

    private int[] suffix;
    private int[] inverseSuffix;
    private int[] lcp;
    private int size;

    public ExtendedSuffixArray(int[] suffix, int[] inverseSuffix, int[] lcp) {
        this.suffix = suffix;
        this.inverseSuffix = inverseSuffix;
        this.lcp = lcp;
        this.size = suffix.length;
    }

    public int[] getSuffix() {
        return suffix;
    }

    public int[] getInverseSuffix() {
        return inverseSuffix;
    }

    public int[] getLcp() {
        return lcp;
    }

    public int getRank(int suffixIndex) {
        return inverseSuffix[suffixIndex];
    }

    public int getLCPValue(int suffixIndex) {
        return lcp[getRank(suffixIndex)];
    }

    public int getLCPMatchIndex(int suffixIndex) {
        return suffix[getRank(suffixIndex) - 1];
    }

    public int getPreceedingSuffixLCPValue(int suffixIndex) {
        return lcp[getRank(suffixIndex - 1)];
    }

    public int size() {
        return size;
    }
}
