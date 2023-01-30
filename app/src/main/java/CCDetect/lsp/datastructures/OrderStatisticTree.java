package CCDetect.lsp.datastructures;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements an order statistic tree which is based on AVL-trees.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 11, 2016)
 * @param the actual element type.
 */
public class OrderStatisticTree {

    public static final class Node {

        Node parent;
        Node left;
        Node right;
        Node link;

        // Just for debugging
        int key = -1;

        int height;
        int count = 0;

        Node() {
        }

        Node(int key) {
            this.key = key;
        }

        public Node getLink() {
            return link;
        }

        public void setLink(Node link) {
            this.link = link;
        }

        public int rank() {
            return count;
        }

        public boolean isRightChild() {
            return parent != null && parent.right == this;
        }

        public boolean isLeftChild() {
            return parent != null && parent.left == this;
        }

        @Override
        public String toString() {
            String out = "Node(" + "rank: " + rank() + ", key:" + (key != -1 ? key : "")
                    + ", link: " + (link != null ? link.key : "");
            if (left != null) {
                out += "\nLeft: " + left.toString();
            }
            if (right != null) {
                out += "\nRight: " + right.toString();
            }
            return out + ")";
        }
    }

    private Node root;
    private int size;
    private int modCount;

    public Node getRoot() {
        return root;
    }

    public Node add(int rank, int label) {
        Node added = add(rank);
        added.key = label;
        return added;
    }

    public Node add(int rank) {

        if (root == null) {
            root = new Node();
            size = 1;
            modCount++;
            return root;
        }

        Node parent = null;
        Node node = root;

        boolean parentLeft = false;
        while (node != null) {
            parent = node;

            if (rank <= node.rank()) {
                parentLeft = true;
                node = node.left;
            } else {
                parentLeft = false;
                rank -= node.rank() + 1;
                node = node.right;
            }
        }

        Node newNode = new Node();

        if (parentLeft) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        newNode.parent = parent;
        size++;
        modCount++;
        Node hi = parent;
        Node lo = newNode;

        while (hi != null) {
            if (hi.left == lo) {
                hi.count++;
            }

            lo = hi;
            hi = hi.parent;
        }

        fixAfterModification(newNode, true);
        return newNode;
    }

    public Node remove(int rank) {

        Node node = root;

        while (node != null) {
            if (rank == node.rank() + 1) {
                break;
            }
            if (rank < node.rank() + 1) {
                node = node.left;
            } else {
                rank -= node.rank() + 1;
                node = node.right;
            }
        }

        if (node == null) {
            return null;
        }

        node = deleteNode(node);
        fixAfterModification(node, false);
        size--;
        modCount++;
        return node;
    }

    public Node getByRank(int rank) {
        Node current = root;
        while (current != null) {
            int currentRank = current.rank();

            if (currentRank == rank) {
                break;
            }

            if (rank < currentRank) {
                current = current.left;
            }

            if (rank > currentRank) {
                current = current.right;
                rank -= currentRank + 1;
            }

        }
        return current;

    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        modCount += size;
        root = null;
        size = 0;
    }

    private void checkIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(
                    "The input index is negative: " + index);
        }

        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "The input index is too large: " + index +
                            ", the size of this tree is " + size);
        }
    }

    public Node deleteByNode(Node node) {

        node = deleteNode(node);
        fixAfterModification(node, false);
        size--;
        modCount++;
        return node;
    }

    private Node deleteNode(Node node) {
        if (node.left == null && node.right == null) {
            // 'node' has no children.
            Node parent = node.parent;

            if (parent == null) {
                // 'node' is the root node of this tree.
                root = null;
                ++modCount;
                return node;
            }

            Node lo = node;
            Node hi = parent;

            while (hi != null) {
                if (hi.left == lo) {
                    hi.count--;
                }

                lo = hi;
                hi = hi.parent;
            }

            if (node == parent.left) {
                parent.left = null;
            } else {
                parent.right = null;
            }

            return node;
        }

        if (node.left != null && node.right != null) {
            // 'node' has both children.

            Node successor = minimumNode(node.right);
            int tmpKey = node.key;
            node.key = successor.key;

            if (successor.getLink() != null) {
                successor.getLink().setLink(node);
                node.setLink(successor.getLink());
            }
            node.key = successor.key;

            Node child = successor.right;
            Node parent = successor.parent;

            if (parent.left == successor) {
                parent.left = child;
            } else {
                parent.right = child;
            }

            if (child != null) {
                child.parent = parent;
            }

            Node lo = child;
            Node hi = parent;

            while (hi != null) {
                if (hi.left == lo) {
                    hi.count--;
                }

                lo = hi;
                hi = hi.parent;
            }

            successor.key = tmpKey;
            return successor;
        }

        Node child;

        // 'node' has only one child.
        if (node.left != null) {
            child = node.left;
        } else {
            child = node.right;
        }

        Node parent = node.parent;
        child.parent = parent;

        if (parent == null) {
            root = child;
            return node;
        }

        if (node == parent.left) {
            parent.left = child;
        } else {
            parent.right = child;
        }

        Node hi = parent;
        Node lo = child;

        while (hi != null) {
            if (hi.left == lo) {
                hi.count--;
            }

            lo = hi;
            hi = hi.parent;
        }

        return node;

    }

    private Node minimumNode(Node node) {
        while (node.left != null) {
            node = node.left;
        }

        return node;
    }

    private int height(Node node) {
        return node == null ? -1 : node.height;
    }

    private Node leftRotate(Node node1) {
        Node node2 = node1.right;
        node2.parent = node1.parent;
        node1.parent = node2;
        node1.right = node2.left;
        node2.left = node1;

        if (node1.right != null) {
            node1.right.parent = node1;
        }

        node1.height = Math.max(height(node1.left), height(node1.right)) + 1;
        node2.height = Math.max(height(node2.left), height(node2.right)) + 1;
        node2.count += node1.count + 1;
        return node2;
    }

    private Node rightRotate(Node node1) {
        Node node2 = node1.left;
        node2.parent = node1.parent;
        node1.parent = node2;
        node1.left = node2.right;
        node2.right = node1;

        if (node1.left != null) {
            node1.left.parent = node1;
        }

        node1.height = Math.max(height(node1.left), height(node1.right)) + 1;
        node2.height = Math.max(height(node2.left), height(node2.right)) + 1;
        node1.count -= node2.count + 1;
        return node2;
    }

    private Node rightLeftRotate(Node node1) {
        Node node2 = node1.right;
        node1.right = rightRotate(node2);
        return leftRotate(node1);
    }

    private Node leftRightRotate(Node node1) {
        Node node2 = node1.left;
        node1.left = leftRotate(node2);
        return rightRotate(node1);
    }

    // Fixing an insertion: use insertionMode = true.
    // Fixing a deletion: use insertionMode = false.
    private void fixAfterModification(Node node, boolean insertionMode) {
        Node parent = node.parent;
        Node grandParent;
        Node subTree;

        while (parent != null) {
            if (height(parent.left) == height(parent.right) + 2) {
                grandParent = parent.parent;

                if (height(parent.left.left) >= height(parent.left.right)) {
                    subTree = rightRotate(parent);
                } else {
                    subTree = leftRightRotate(parent);
                }

                if (grandParent == null) {
                    root = subTree;
                } else if (grandParent.left == parent) {
                    grandParent.left = subTree;
                } else {
                    grandParent.right = subTree;
                }

                if (grandParent != null) {
                    grandParent.height = Math.max(
                            height(grandParent.left),
                            height(grandParent.right)) + 1;
                }

                if (insertionMode) {
                    // Whenever fixing after insertion, at most one rotation is
                    // required in order to maintain the balance.
                    return;
                }
            } else if (height(parent.right) == height(parent.left) + 2) {
                grandParent = parent.parent;

                if (height(parent.right.right) >= height(parent.right.left)) {
                    subTree = leftRotate(parent);
                } else {
                    subTree = rightLeftRotate(parent);
                }

                if (grandParent == null) {
                    root = subTree;
                } else if (grandParent.left == parent) {
                    grandParent.left = subTree;
                } else {
                    grandParent.right = subTree;
                }

                if (grandParent != null) {
                    grandParent.height = Math.max(height(grandParent.left),
                            height(grandParent.right)) + 1;
                }

                if (insertionMode) {
                    return;
                }
            }

            parent.height = Math.max(height(parent.left),
                    height(parent.right)) + 1;
            parent = parent.parent;
        }
    }

    public boolean isHealthy() {
        if (root == null) {
            return true;
        }

        return !containsCycles()
                && heightsAreCorrect()
                && isBalanced()
                && isWellIndexed();
    }

    private boolean containsCycles() {
        Set<Node> visitedNodes = new HashSet<>();
        return containsCycles(root, visitedNodes);
    }

    private boolean containsCycles(Node current, Set<Node> visitedNodes) {
        if (current == null) {
            return false;
        }

        if (visitedNodes.contains(current)) {
            return true;
        }

        visitedNodes.add(current);

        return containsCycles(current.left, visitedNodes)
                || containsCycles(current.right, visitedNodes);
    }

    private boolean heightsAreCorrect() {
        return getHeight(root) == root.height;
    }

    private int getHeight(Node node) {
        if (node == null) {
            return -1;
        }

        int leftTreeHeight = getHeight(node.left);

        if (leftTreeHeight == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        int rightTreeHeight = getHeight(node.right);

        if (rightTreeHeight == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        if (node.height == Math.max(leftTreeHeight, rightTreeHeight) + 1) {
            return node.height;
        }

        return Integer.MIN_VALUE;
    }

    private boolean isBalanced() {
        return isBalanced(root);
    }

    private boolean isBalanced(Node node) {
        if (node == null) {
            return true;
        }

        if (!isBalanced(node.left)) {
            return false;
        }

        if (!isBalanced(node.right)) {
            return false;
        }

        int leftHeight = height(node.left);
        int rightHeight = height(node.right);

        return Math.abs(leftHeight - rightHeight) < 2;
    }

    private boolean isWellIndexed() {
        return size == count(root);
    }

    private int count(Node node) {
        if (node == null) {
            return 0;
        }

        int leftTreeSize = count(node.left);

        if (leftTreeSize == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        if (node.count != leftTreeSize) {
            return Integer.MIN_VALUE;
        }

        int rightTreeSize = count(node.right);

        if (rightTreeSize == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return leftTreeSize + 1 + rightTreeSize;
    }
}
