package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.List;

import CCDetect.lsp.datastructures.OrderStatisticTree.Node;
import CCDetect.lsp.datastructures.rankselect.DynamicBitSet;
import CCDetect.lsp.datastructures.rankselect.DynamicTreeBitSet;
import CCDetect.lsp.utils.Printer;

/**
 * DynamicLCP
 */
public class DynamicLCP {

    OrderStatisticTree tree;
    // DynamicTreeBitSet positionsToUpdate;
    public DynamicTreeBitSet positionsToUpdate;

    public DynamicLCP(int[] initial) {
        tree = new OrderStatisticTree();
        for (int i = 0; i < initial.length; i++) {
            tree.addWithKey(i, initial[i]);
        }

        // positionsToUpdate = new DynamicTreeBitSet(initial.length);
        positionsToUpdate = new DynamicTreeBitSet(initial.length + 20);
    }

    public int get(int index) {
        return tree.getByRank(index).key;
    }

    // Inserts new node which will later be set to the correct value
    public void insertNewValue(int index) {
        System.out.println("new value at " + index + " and " + (index + 1));
        positionsToUpdate.set(index, true);
        positionsToUpdate.set(index + 1, true);
        tree.addWithKey(index, 0);
    }

    public void delete(int index) {
        positionsToUpdate.set(index + 1, false);
        tree.deleteByNode(tree.getByRank(index));

        positionsToUpdate.set(index, true);
    }

    public void deleteValue(int index) {
        System.out.println("LCP value deleted at " + index);
        positionsToUpdate.set(index, true);
        tree.remove(index);
    }

    public void setValue(int index, int value) {
        tree.getByRank(index).key = value;
    }

    public List<Integer> getPositionsToUpdate() {
        System.out.println("Positions to update bitset: " + Printer.print(positionsToUpdate.toBitSet()));
        List<Integer> positions = new ArrayList<>();
        int rank = 0;
        int current = positionsToUpdate.select(rank, true);
        while (current != -1) {
            positions.add(current);
            rank++;
            current = positionsToUpdate.select(rank, true);
        }
        return positions;
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

    public void insert(int index, int value) {
        tree.addWithKey(index, value);
    }
}
