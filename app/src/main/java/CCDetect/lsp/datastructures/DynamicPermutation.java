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
            // System.out.println("i: " + i);
            // System.out.println("arr[i]: " + initial[i]);
            // System.out.println("aTree: " + aTree.getRoot());
            // System.out.println("bTree: " + bTree.getRoot());
            Node aNode = aTree.getByRank(i);
            Node bNode = bTree.getByRank(initial[i]);
            // System.out.println("aNode: " + aNode);
            // System.out.println("bNode: " + bNode);
            aNode.setLink(bNode);
            bNode.setLink(aNode);
        }
    }

    private void insertInitial(int index, int element) {
        aTree.add(index, index);
        bTree.add(element, element);
    }

    public void insert(int index, int element) {
        Node aNode = aTree.add(index, index);
        Node bNode = bTree.add(element, element);
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
        for (int i = 0; i < aTree.size(); i++) {
            out[i] = get(i);
        }
        return out;
    }
}
