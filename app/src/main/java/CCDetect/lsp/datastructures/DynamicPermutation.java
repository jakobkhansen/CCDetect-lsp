package CCDetect.lsp.datastructures;

import CCDetect.lsp.datastructures.OrderStatisticTree.Node;

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
            Node aNode = aTree.getByRank(i);
            Node bNode = bTree.getByRank(initial[i]);
            aNode.setLink(bNode);
            bNode.setLink(aNode);
        }
    }

    private void insertInitial(int index, int element) {
        aTree.add(index);
        bTree.add(element);
    }

    public void insert(int index, int element) {
        Node aNode = aTree.add(index);
        Node bNode = bTree.add(element);
        aNode.setLink(bNode);
        bNode.setLink(aNode);
    }

    public void delete(int index) {

        Node aNode = aTree.getByRank(index);
        Node bNode = aNode.getLink();
        aTree.deleteByNode(aNode);
        bTree.deleteByNode(bNode);
    }

    public int get(int index) {
        Node aNode = aTree.getByRank(index);
        Node bNode = aNode.getLink();

        return inOrderRank(bNode);
    }

    public int getInverse(int index) {
        Node bNode = bTree.getByRank(index);
        Node aNode = bNode.getLink();

        return inOrderRank(aNode);
    }

    public static int inOrderRank(Node node) {
        int rank = node.rank();
        while (node.parent != null) {
            if (node.isRightChild()) {
                rank += node.parent.rank() + 1;
            }
            node = node.parent;
        }
        return rank;
    }

    public int[] toArray() {
        int[] out = new int[aTree.size()];
        int inorderRank = 0;
        for (Node node : bTree) {
            node.inorderRank = inorderRank;
            inorderRank++;
        }
        int index = 0;
        for (Node node : aTree) {
            out[index] = node.getLink().inorderRank;
            index++;
        }
        return out;
    }

    public int[] inverseToArray() {
        int[] out = new int[aTree.size()];
        int inorderRank = 0;
        for (Node node : aTree) {
            node.inorderRank = inorderRank;
            inorderRank++;
        }
        int index = 0;
        for (Node node : bTree) {
            out[index] = node.getLink().inorderRank;
            index++;
        }
        return out;
    }

    public int size() {
        return aTree.size();
    }
}
