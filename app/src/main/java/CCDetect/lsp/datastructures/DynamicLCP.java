package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import CCDetect.lsp.datastructures.OrderStatisticTree.OSTreeNode;
import CCDetect.lsp.datastructures.rankselect.DynamicTreeBitSet;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.utils.Printer;

/**
 * DynamicLCP
 */
public class DynamicLCP {

    int CLONE_THRESHOLD = Configuration.getInstance().getCloneTokenThreshold();
    OrderStatisticTree tree;
    public DynamicTreeBitSet positionsToUpdate;
    List<OSTreeNode> nodesAboveThreshold = new ArrayList<>();

    public DynamicLCP(int[] initial) {
        tree = new OrderStatisticTree();
        for (int i = 0; i < initial.length; i++) {
            add(i, initial[i]);
        }

        positionsToUpdate = new DynamicTreeBitSet(initial.length);
    }

    public void add(int index, int value) {
        OSTreeNode node = tree.addWithKey(index, value);
        if (value >= CLONE_THRESHOLD) {
            nodesAboveThreshold.add(node);
        }
    }

    public int get(int index) {
        return tree.getByRank(index).key;
    }

    public OSTreeNode getNode(int index) {
        return tree.getByRank(index);
    }

    // Inserts new node which will later be set to the correct value
    public OSTreeNode insertNewNode(int index) {
        positionsToUpdate.insert(index, true);
        positionsToUpdate.set(index + 1, true);
        return tree.addWithKey(index, -1);
    }

    public OSTreeNode deleteValue(int index) {
        if (index > 1) {

            positionsToUpdate.set(index - 1, true);
        }
        positionsToUpdate.set(index + 1, true);
        positionsToUpdate.delete(index);
        OSTreeNode deletedNode = tree.remove(index);
        deletedNode.key = -1;
        return deletedNode;
    }

    public void setValue(int index, int value) {
        OSTreeNode node = tree.getByRank(index);
        int oldValue = node.key;
        node.key = value;

        if (oldValue < CLONE_THRESHOLD && value >= CLONE_THRESHOLD) {
            nodesAboveThreshold.add(node);
        }
    }

    public List<OSTreeNode> getNodesAboveThreshold() {
        nodesAboveThreshold.removeIf(node -> node.key < CLONE_THRESHOLD);

        return nodesAboveThreshold;
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
        Iterator<OSTreeNode> lcpIterator = tree.iterator();
        Iterator<OSTreeNode> saIterator = permutation.aTree.iterator();
        while (lcpIterator.hasNext() && saIterator.hasNext()) {
            lcpIterator.next().setInverseLink(saIterator.next());
        }
    }

    public int[] toArray() {
        int[] out = new int[tree.size()];
        int i = 0;
        for (OSTreeNode node : tree) {
            out[i] = node.key;
            i++;
        }
        return out;
    }

    public void insert(int index, int value) {
        tree.addWithKey(index, value);
    }
}
