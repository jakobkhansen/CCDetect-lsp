package CCDetect.lsp.datastructures.rankselect;

import java.util.BitSet;

/**
 * DynamicBitSet
 */
public class DynamicBitSet {

    BitSet set;
    int realSize = 0;

    public DynamicBitSet(int initialSize, int availableSpace) {
        set = new BitSet(availableSpace);
        this.realSize = initialSize;
    }

    public void set(int index) {
        set.set(index);
    }

    public void set(int index, boolean value) {
        set.set(index, value);
    }

    public void clear(int index) {
        set.clear(index);
    }

    public boolean get(int index) {
        return set.get(index);
    }

    public void insert(int index, boolean value) {
        realSize++;
        for (int i = realSize - 1; i > index; i--) {
            set(i, get(i - 1));
        }
        set(index, value);
    }

    public int size() {
        return realSize;
    }

    public int rank(int index, boolean value) {

        int res = 0;
        for (int i = 0; i < index; i++) {
            res += get(i) == value ? 1 : 0;
        }
        return res;
    }

    public int getNumZeroes() {
        return realSize - set.cardinality();
    }

    private int availableSpace() {
        return set.size();
    }
}
