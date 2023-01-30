package CCDetect.lsp.datastructures;

/**
 * DynamicExtendedSuffixArray
 */
public class DynamicExtendedSuffixArray extends ExtendedSuffixArray {

    private int[] lcp;
    private DynamicPermutation sa;

    public DynamicExtendedSuffixArray(DynamicPermutation sa, int[] lcp) {
        super();
        this.sa = sa;
        this.lcp = lcp;
    }

    @Override
    public int getLCPMatchIndex(int suffixIndex) {
        return sa.get(getRank(suffixIndex) - 1);
    }

    @Override
    public int getLCPValue(int suffixIndex) {
        return lcp[getRank(suffixIndex)];
    }

    @Override
    public int[] getLcp() {
        return lcp;
    }

    @Override
    public int getPreceedingSuffixLCPValue(int suffixIndex) {
        return lcp[getRank(suffixIndex - 1)];
    }

    @Override
    public int getRank(int suffixIndex) {
        return sa.getInverse(suffixIndex);
    }

    @Override
    public int[] getSuffix() {
        int[] suff = new int[sa.size()];
        for (int i = 0; i < suff.length; i++) {
            suff[i] = sa.get(i);
        }
        return suff;
    }

    @Override
    public int[] getInverseSuffix() {
        int[] inverse = new int[sa.size()];
        for (int i = 0; i < inverse.length; i++) {
            inverse[i] = sa.getInverse(i);
        }
        return inverse;
    }

    @Override
    public int size() {
        return sa.size();
    }

}
