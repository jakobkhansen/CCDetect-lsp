package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.List;

import CCDetect.lsp.datastructures.OrderStatisticTree.Node;

/**
 * DynamicLCP
 */
public class DynamicLCP {

    OrderStatisticTree tree;
    // DynamicTreeBitSet positionsToUpdate;
    List<Integer> positionsToUpdate;

    public DynamicLCP(int[] initial) {
        tree = new OrderStatisticTree();
        for (int i = 0; i < initial.length; i++) {
            tree.addWithKey(i, initial[i]);
        }

        // positionsToUpdate = new DynamicTreeBitSet(initial.length);
        positionsToUpdate = new ArrayList<>();
    }

    public int get(int index) {
        return tree.getByRank(index).key;
    }

    public void addPositionToUpdate(int index) {
        // positionsToUpdate.set(index, true);
        positionsToUpdate.add(index);
    }

    public List<Integer> getPositionsToUpdate() {
        return positionsToUpdate;
    }

    public int[] toArray() {
        int[] out = new int[tree.size()];
        int i = 0;
        for (Node node : tree) {
            out[i] = node.key;
            i++;
        }
        return out;
    }
}
