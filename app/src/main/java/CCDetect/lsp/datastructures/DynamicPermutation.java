package CCDetect.lsp.datastructures;

import CCDetect.lsp.datastructures.OrderStatisticTree.OSTreeNode;

/**
 * DynamicPermutation
 * Stores dynamic permutation of the range (0, n-1)
 * Allows inserts/deletes which increments/decrements elements geq
 */
public class DynamicPermutation {

    public OrderStatisticTree aTree, bTree;

    public DynamicPermutation(int[] initial) {
        aTree = new OrderStatisticTree();
        bTree = new OrderStatisticTree();

        for (int i = 0; i < initial.length; i++) {
            insertInitial(i, i);
        }

        for (int i = 0; i < initial.length; i++) {
            OSTreeNode aNode = aTree.getByRank(i);
            OSTreeNode bNode = bTree.getByRank(initial[i]);
            aNode.setInverseLink(bNode);
            bNode.setInverseLink(aNode);
        }
    }

    private void insertInitial(int index, int element) {
        aTree.add(index);
        bTree.add(element);
    }

    public OSTreeNode insert(int index, int element) {
        OSTreeNode aNode = aTree.add(index);
        OSTreeNode bNode = bTree.add(element);
        aNode.setInverseLink(bNode);
        bNode.setInverseLink(aNode);

        return aNode;
    }

    // Returns aNode which has a link to bNode
    public OSTreeNode delete(int index) {

        OSTreeNode aNode = aTree.getByRank(index);
        OSTreeNode bNode = aNode.getInverseLink();
        aTree.deleteByNode(aNode);
        bTree.deleteByNode(bNode);

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

    public int size() {
        return aTree.size();
    }
}
