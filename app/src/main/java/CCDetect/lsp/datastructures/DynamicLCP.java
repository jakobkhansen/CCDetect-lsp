package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import CCDetect.lsp.datastructures.OrderStatisticTree.Node;
import CCDetect.lsp.datastructures.rankselect.DynamicTreeBitSet;
import CCDetect.lsp.utils.Printer;

/**
 * DynamicLCP
 */
public class DynamicLCP {

    OrderStatisticTree tree;
    public DynamicTreeBitSet positionsToUpdate;

    public DynamicLCP(int[] initial) {
        tree = new OrderStatisticTree();
        for (int i = 0; i < initial.length; i++) {
            tree.addWithKey(i, initial[i]);
        }

        positionsToUpdate = new DynamicTreeBitSet(initial.length);
    }

    public int get(int index) {
        return tree.getByRank(index).key;
    }

    // Inserts new node which will later be set to the correct value
    public void insertNewValue(int index) {
        positionsToUpdate.insert(index, true);
        positionsToUpdate.set(index + 1, true);
        tree.addWithKey(index, -1);
    }

    public void deleteValue(int index) {
        if (index > 1) {

            positionsToUpdate.set(index - 1, true);
        }
        positionsToUpdate.set(index + 1, true);
        positionsToUpdate.delete(index);
        tree.remove(index);
    }

    public void setValue(int index, int value) {
        tree.getByRank(index).key = value;
    }

    public List<Integer> getPositionsToUpdate() {
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

    public void setLinks(DynamicPermutation permutation) {
        Iterator<Node> lcpIterator = tree.iterator();
        Iterator<Node> saIterator = permutation.aTree.iterator();
        while (lcpIterator.hasNext() && saIterator.hasNext()) {
            lcpIterator.next().setLink(saIterator.next());
        }
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
