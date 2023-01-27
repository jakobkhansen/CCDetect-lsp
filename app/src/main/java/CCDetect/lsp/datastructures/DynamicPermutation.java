package CCDetect.lsp.datastructures;

import CCDetect.lsp.datastructures.RedBlackTree.Node;

/**
 * DynamicPermutation
 * Stores dynamic permutation of the range (0, n-1)
 * Allows inserts/deletes which increments/decrements elements geq
 */
public class DynamicPermutation {

    RedBlackTree aTree, bTree;

    public DynamicPermutation(int[] initial) {
        aTree = new RedBlackTree();
        bTree = new RedBlackTree();

        for (int i = 0; i < initial.length; i++) {
            insert(i, initial[i]);
        }
        System.out.println(bTree.root);
        System.out.println();
    }

    public void insert(int index, int element) {

        Node aNode = aTree.insert(index + 1);
        System.out.println("a node: " + aNode.value);
        Node bNode = bTree.insert(element + 1);
        System.out.println("b node: " + bNode.value);
        aNode.link = bNode;
    }

    public int get(int index) {
        Node aNode = aTree.getByRank(index + 1);
        Node bNode = aNode.link;

        return inOrderRank(bNode) - 1;
    }

    public int inOrderRank(Node node) {
        int rank = node.rank();
        while (node != bTree.root) {
            if (node.isRightChild()) {
                rank += node.parent.rank();
            }
            node = node.parent;
        }
        return rank;
    }
}
