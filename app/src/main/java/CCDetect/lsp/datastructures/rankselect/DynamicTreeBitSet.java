package CCDetect.lsp.datastructures.rankselect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.LinkedList;

import CCDetect.lsp.utils.Printer;

/**
 * DynamicTreeBitSet
 */
public class DynamicTreeBitSet {

    int size, slowDownFactor;
    Node root;

    // Node is either a inner node or leaf, If leaf, has bitset, if inner has
    // subtrees.
    public class Node {
        int bitsInLeft, setInLeft;
        Node left, right;
        DynamicBitSet bitSet;

        public Node(Node left, Node right) {
            this.left = left;
            this.right = right;

            this.bitsInLeft = left.getNumBits();
            this.setInLeft = left.getNumSetBits();
        }

        public Node(int bitSetSize) {
            this.bitSet = new DynamicBitSet(bitSetSize, bitSetSize * 2);
        }

        public int getNumBits() {
            if (isLeaf()) {
                return bitSet.size();
            }
            return bitsInLeft + right.getNumBits();
        }

        public int getNumSetBits() {
            if (isLeaf()) {
                return bitSet.getNumOnes();
            }
            return setInLeft + right.getNumSetBits();
        }

        public int getNumSetBitsInLeft() {
            return setInLeft;
        }

        public int getNumBitsInLeft() {
            return bitsInLeft;
        }

        public boolean isLeaf() {
            return bitSet != null;
        }

        // Returns whether or not the parent should increment its set bit counter
        public int setBit(int index, boolean value) {
            if (isLeaf()) {
                int retValue = 0;
                if (!value && bitSet.get(index)) {
                    retValue = -1;
                }

                if (value && !bitSet.get(index)) {
                    retValue = 1;
                }

                bitSet.set(index, value);
                return retValue;
            }

            if (index < bitsInLeft) {
                int retValue = left.setBit(index, value);
                setInLeft += retValue;
                return retValue;
            }

            return right.setBit(index - bitsInLeft, value);
        }

        public boolean get(int index) {
            if (isLeaf()) {
                return bitSet.get(index);
            }

            if (index < bitsInLeft) {
                return left.get(index);
            }

            return right.get(index - bitsInLeft);
        }

        public int rank(int index, boolean value) {
            if (isLeaf()) {
                return bitSet.rank(index, value);
            }

            if (index < bitsInLeft) {
                return left.rank(index, value);
            }

            int equalBitsInLeft = value ? setInLeft : (bitsInLeft - setInLeft);
            return equalBitsInLeft + right.rank(index - bitsInLeft, value);
        }

        public void insert(int index, boolean value) {
            if (isLeaf()) {
                bitSet.insert(index, value);
                return;
            }

            if (index < bitsInLeft) {
                bitsInLeft++;
                setInLeft += value ? 1 : 0;
                left.insert(index, value);
                return;
            }

            right.insert(index - bitsInLeft, value);
        }

        // Returns the value deleted, also used to decrement set bit counter
        public boolean delete(int index) {
            if (isLeaf()) {
                return bitSet.delete(index);
            }

            if (index < bitsInLeft) {
                boolean retVal = left.delete(index);
                bitsInLeft--;
                setInLeft -= retVal ? 1 : 0;
                return retVal;
            }

            return right.delete(index - bitsInLeft);
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder("Node(");

            if (isLeaf()) {
                out.append(Printer.print(bitSet) + "\n");
            } else {
                out.append("(bitsInLeft=" + bitsInLeft + ", setInLeft= " + setInLeft + ")");
                out.append("\t" + left.toString() + right.toString());
                out.append(")\n");
            }

            return out.toString();
        }
    }

    public DynamicTreeBitSet(int initialSize) {
        size = initialSize;
        slowDownFactor = (int) Math.ceil((Math.log(initialSize) / Math.log(2)));
        slowDownFactor = Math.max(1, slowDownFactor);
        buildTree();
    }

    public void buildTree() {
        Deque<Node> queue = new LinkedList<>();

        for (int i = 0; i < size; i += slowDownFactor) {
            Node leafNode = new Node(slowDownFactor);
            queue.push(leafNode);
        }
        while (queue.size() > 1) {
            Node left = queue.removeFirst();
            Node right = queue.removeFirst();
            Node parent = new Node(left, right);
            queue.addLast(parent);
        }
        root = queue.pop();
    }

    public int rank(int index, boolean value) {
        return root.rank(index, value);
    }

    public void set(int index, boolean value) {
        root.setBit(index, value);
    }

    public boolean get(int index) {
        return root.get(index);
    }

    public void insert(int index, boolean value) {
        root.insert(index, value);
        size++;
    }

    public void delete(int index) {
        root.delete(index);
        size--;
    }

    // Should return number of zeroes in entire bitset
    // Should be easy, just return the number at the root or traverse to the bottom
    // right or smth. Could also track these numbers
    public int getNumZeroes() {
        return size - root.getNumSetBits();
    }

    public Node getRoot() {
        return root;
    }

    public DynamicBitSet toBitSet() {
        DynamicBitSet out = new DynamicBitSet(size, size);
        for (int i = 0; i < size; i++) {
            out.set(i, get(i));
        }
        return out;
    }
}