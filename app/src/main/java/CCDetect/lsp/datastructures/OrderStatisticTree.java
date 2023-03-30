package CCDetect.lsp.datastructures;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class implements an order statistic tree which is based on AVL-trees.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 11, 2016)
 */
public class OrderStatisticTree implements Iterable<OrderStatisticTree.OSTreeNode> {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public static final class OSTreeNode {

        OSTreeNode parent;
        OSTreeNode left;
        OSTreeNode right;
        OSTreeNode inverseLink;

        public int key = -1;

        // Used when building int[] sa in DynamicPermutation.toArray;
        int inorderRank;

        int height;
        int count = 0;

        OSTreeNode() {
        }

        OSTreeNode(int key) {
            this.key = key;
        }

        public OSTreeNode getInverseLink() {
            return inverseLink;
        }

        public void setInverseLink(OSTreeNode link) {
            this.inverseLink = link;
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
            String out = "Node(" + "rank: " + rank() + ", key:" + key + "inorder rank: " + inOrderRank(this) + ")";
            return out;
        }
    }

    private OSTreeNode root;
    private int size;
    private int modCount;

    public OSTreeNode getRoot() {
        return root;
    }

    public OSTreeNode addWithKey(int rank, int key) {
        OSTreeNode added = add(rank);
        added.key = key;
        return added;
    }

    public OSTreeNode add(int rank) {

        if (root == null) {
            root = new OSTreeNode();
            size = 1;
            modCount++;
            return root;
        }

        OSTreeNode parent = null;
        OSTreeNode node = root;

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

        OSTreeNode newNode = new OSTreeNode();

        if (parentLeft) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        newNode.parent = parent;
        size++;
        modCount++;
        OSTreeNode hi = parent;
        OSTreeNode lo = newNode;

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

    public OSTreeNode remove(int rank) {

        OSTreeNode node = root;

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

    public OSTreeNode getByRank(int rank) {
        OSTreeNode current = root;
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

    public OSTreeNode deleteByNode(OSTreeNode node) {

        node = deleteNode(node);
        fixAfterModification(node, false);
        size--;
        modCount++;
        return node;
    }

    private OSTreeNode deleteNode(OSTreeNode node) {
        if (node.left == null && node.right == null) {
            // 'node' has no children.
            OSTreeNode parent = node.parent;

            if (parent == null) {
                // 'node' is the root node of this tree.
                root = null;
                ++modCount;
                return node;
            }

            OSTreeNode lo = node;
            OSTreeNode hi = parent;

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

            OSTreeNode successor = minimumNode(node.right);
            int tmpKey = node.key;
            node.key = successor.key;

            if (successor.getInverseLink() != null) {
                successor.getInverseLink().setInverseLink(node);
                node.setInverseLink(successor.getInverseLink());
            }

            node.key = successor.key;

            OSTreeNode child = successor.right;
            OSTreeNode parent = successor.parent;

            if (parent.left == successor) {
                parent.left = child;
            } else {
                parent.right = child;
            }

            if (child != null) {
                child.parent = parent;
            }

            OSTreeNode lo = child;
            OSTreeNode hi = parent;

            while (hi != null) {
                if (hi.left == lo) {
                    hi.count--;
                }

                lo = hi;
                hi = hi.parent;
            }

            successor.key = -1;
            return successor;
        }

        OSTreeNode child;

        // 'node' has only one child.
        if (node.left != null) {
            child = node.left;
        } else {
            child = node.right;
        }

        OSTreeNode parent = node.parent;
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

        OSTreeNode hi = parent;
        OSTreeNode lo = child;

        while (hi != null) {
            if (hi.left == lo) {
                hi.count--;
            }

            lo = hi;
            hi = hi.parent;
        }

        return node;

    }

    private OSTreeNode minimumNode(OSTreeNode node) {
        while (node.left != null) {
            node = node.left;
        }

        return node;
    }

    private int height(OSTreeNode node) {
        return node == null ? -1 : node.height;
    }

    private OSTreeNode leftRotate(OSTreeNode node1) {
        OSTreeNode node2 = node1.right;
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

    private OSTreeNode rightRotate(OSTreeNode node1) {
        OSTreeNode node2 = node1.left;
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

    private OSTreeNode rightLeftRotate(OSTreeNode node1) {
        OSTreeNode node2 = node1.right;
        node1.right = rightRotate(node2);
        return leftRotate(node1);
    }

    private OSTreeNode leftRightRotate(OSTreeNode node1) {
        OSTreeNode node2 = node1.left;
        node1.left = leftRotate(node2);
        return rightRotate(node1);
    }

    // Fixing an insertion: use insertionMode = true.
    // Fixing a deletion: use insertionMode = false.
    private void fixAfterModification(OSTreeNode node, boolean insertionMode) {
        OSTreeNode parent = node.parent;
        OSTreeNode grandParent;
        OSTreeNode subTree;

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
        Set<OSTreeNode> visitedNodes = new HashSet<>();
        return containsCycles(root, visitedNodes);
    }

    private boolean containsCycles(OSTreeNode current, Set<OSTreeNode> visitedNodes) {
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

    private int getHeight(OSTreeNode node) {
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

    private boolean isBalanced(OSTreeNode node) {
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

    private int count(OSTreeNode node) {
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

    @Override
    public Iterator<OSTreeNode> iterator() {
        // TODO Auto-generated method stub
        return new TreeIterator();
    }

    private final class TreeIterator implements Iterator<OSTreeNode> {

        private OSTreeNode previousNode;
        private OSTreeNode nextNode;
        private int expectedModCount = modCount;

        TreeIterator() {
            if (root == null) {
                nextNode = null;
            } else {
                nextNode = minimumNode(root);
            }
        }

        @Override
        public boolean hasNext() {
            return nextNode != null;
        }

        @Override
        public OSTreeNode next() {
            if (nextNode == null) {
                throw new NoSuchElementException("Iteration exceeded.");
            }

            checkConcurrentModification();
            OSTreeNode datum = nextNode;
            previousNode = nextNode;
            nextNode = successorOf(nextNode);
            return datum;
        }

        @Override
        public void remove() {
            if (previousNode == null) {
                throw new IllegalStateException(
                        nextNode == null ? "Not a single call to next(); nothing to remove."
                                : "Removing the same element twice.");
            }

            checkConcurrentModification();

            OSTreeNode x = deleteNode(previousNode);
            fixAfterModification(x, false);

            if (x == nextNode) {
                nextNode = previousNode;
            }

            expectedModCount = ++modCount;
            size--;
            previousNode = null;
        }

        private void checkConcurrentModification() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException(
                        "The set was modified while iterating.");
            }
        }
    }

    private static OSTreeNode successorOf(OSTreeNode node) {
        if (node.right != null) {
            node = node.right;

            while (node.left != null) {
                node = node.left;
            }

            return node;
        }

        OSTreeNode parent = node.parent;

        while (parent != null && parent.right == node) {
            node = parent;
            parent = parent.parent;
        }

        return parent;
    }

    public static OSTreeNode predecessorOf(OSTreeNode node) {
        if (node.left != null) {
            node = node.left;

            while (node.right != null) {
                node = node.right;
            }

            return node;
        }

        OSTreeNode parent = node.parent;
        while (parent != null && parent.left == node) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    public static int inOrderRank(OSTreeNode node) {
        int rank = node.rank();
        while (node.parent != null) {
            if (node.isRightChild()) {
                rank += node.parent.rank() + 1;
            }
            node = node.parent;
        }
        return rank;
    }

}
