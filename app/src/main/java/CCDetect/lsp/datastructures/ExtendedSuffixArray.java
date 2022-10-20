package CCDetect.lsp.datastructures;

/**
 * ExtendedSuffixArray
 */
public class ExtendedSuffixArray {

    private int[] suffix;
    private int[] inverseSuffix;
    private int[] lcp;

    public ExtendedSuffixArray(int[] suffix, int[] inverseSuffix, int[] lcp) {
        this.suffix = suffix;
        this.inverseSuffix = inverseSuffix;
        this.lcp = lcp;
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
}
