package CCDetect.lsp.datastructures;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import CCDetect.lsp.datastructures.OrderStatisticTree.OSTreeNode;
import CCDetect.lsp.datastructures.rankselect.DynamicTreeBitSet;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.utils.Printer;

/**
 * DynamicPermutation
 * Stores dynamic permutation of the range (0, n-1)
 * Allows inserts/deletes which increments/decrements elements geq
 */
public class DynamicPermutation {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    int CLONE_THRESHOLD = Configuration.getInstance().getCloneTokenThreshold();

    public OrderStatisticTree aTree, bTree;

    // LCP
    public DynamicTreeBitSet positionsToUpdate;
    List<OSTreeNode> nodesAboveThreshold = new LinkedList<>();

    public DynamicPermutation(int[] initialSA, int[] initialLCP) {
        aTree = new OrderStatisticTree();
        bTree = new OrderStatisticTree();

        positionsToUpdate = new DynamicTreeBitSet(initialSA.length);

        for (int i = 0; i < initialSA.length; i++) {
            insertInitial(i, i, initialLCP[i]);
        }

        for (int i = 0; i < initialSA.length; i++) {
            OSTreeNode aNode = aTree.getByRank(i);
            OSTreeNode bNode = bTree.getByRank(initialSA[i]);
            aNode.setInverseLink(bNode);
            bNode.setInverseLink(aNode);
        }
    }

    private void insertInitial(int index, int element, int lcpValue) {
        OSTreeNode aNode = aTree.addWithKey(index, lcpValue);
        bTree.add(element);

        if (lcpValue >= CLONE_THRESHOLD) {
            nodesAboveThreshold.add(aNode);
        }
    }

    public OSTreeNode insert(int index, int element, int lcpValue) {
        OSTreeNode aNode = aTree.addWithKey(index, lcpValue);
        OSTreeNode bNode = bTree.add(element);
        aNode.setInverseLink(bNode);
        bNode.setInverseLink(aNode);

        positionsToUpdate.insert(index, true);
        positionsToUpdate.set(index + 1, true);

        if (lcpValue >= CLONE_THRESHOLD) {
            nodesAboveThreshold.add(aNode);
        }

        return aNode;
    }

    // Returns aNode which has a link to bNode
    public OSTreeNode delete(int index) {

        OSTreeNode aNode = aTree.getByRank(index);
        OSTreeNode bNode = aNode.getInverseLink();
        aTree.deleteByNode(aNode);
        bTree.deleteByNode(bNode);

        if (index > 1) {
            positionsToUpdate.set(index - 1, true);
        }
        positionsToUpdate.set(index, true);
        positionsToUpdate.set(index + 1, true);
        positionsToUpdate.delete(index);

        aNode.key = -1;

        return aNode;
    }

    public int get(int index) {
        OSTreeNode aNode = aTree.getByRank(index);
        OSTreeNode bNode = aNode.getInverseLink();

        return OrderStatisticTree.inOrderRank(bNode);
    }

    public OSTreeNode getNode(int index) {
        OSTreeNode aNode = aTree.getByRank(index);
        OSTreeNode bNode = aNode.getInverseLink();

        return bNode;
    }

    public int getInverse(int index) {
        OSTreeNode bNode = bTree.getByRank(index);
        OSTreeNode aNode = bNode.getInverseLink();

        return OrderStatisticTree.inOrderRank(aNode);
    }

    public void setLCPValue(int index, int value) {

        OSTreeNode node = aTree.getByRank(index);
        int oldValue = node.key;
        node.key = value;

        if (oldValue < CLONE_THRESHOLD && value >= CLONE_THRESHOLD) {
            nodesAboveThreshold.add(node);
        }
    }

    public int getLCPValue(int index) {
        return aTree.getByRank(index).key;
    }

    public List<OSTreeNode> getNodesAboveThreshold() {
        nodesAboveThreshold.removeIf(node -> node.key < CLONE_THRESHOLD);

        return nodesAboveThreshold;
    }

    public OSTreeNode getInverseNode(int index) {
        OSTreeNode bNode = bTree.getByRank(index);
        OSTreeNode aNode = bNode.getInverseLink();

        return aNode;
    }

    public OSTreeNode getNthNode(int index) {
        return aTree.getByRank(index);
    }

    public OSTreeNode getNthInverseNode(int index) {
        return bTree.getByRank(index);
    }

    public int[] toArray() {
        int[] out = new int[aTree.size()];
        int inorderRank = 0;
        for (OSTreeNode node : bTree) {
            node.inorderRank = inorderRank;
            inorderRank++;
        }
        int index = 0;
        for (OSTreeNode node : aTree) {
            out[index] = node.getInverseLink().inorderRank;
            index++;
        }
        return out;
    }

    public int[] inverseToArray() {
        int[] out = new int[aTree.size()];
        int inorderRank = 0;
        for (OSTreeNode node : aTree) {
            node.inorderRank = inorderRank;
            inorderRank++;
        }
        int index = 0;
        for (OSTreeNode node : bTree) {
            out[index] = node.getInverseLink().inorderRank;
            index++;
        }
        return out;
    }

    public int[] lcpToArray() {
        int[] out = new int[aTree.size()];
        int index = 0;
        for (OSTreeNode node : aTree) {
            out[index] = node.key;
            index++;
        }
        return out;
    }

    public int size() {
        return aTree.size();
    }

}
