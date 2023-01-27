package CCDetect.lsp.datastructures;

public class RedBlackTree {
    public Node root;

    public static final boolean RED = true;
    public static final boolean BLACK = false;

    public class Node {
        public int value;
        public Node parent, left, right, link;
        public boolean color = RED;

        public Node(int value, Node parent) {
            this.value = value;
            this.parent = parent;
        }

        public Node getUncle() {
            return parent.getSibling();
        }

        public Node getSibling() {
            if (parent.left == this) {
                return parent.right;
            }

            return parent.left;
        }

        public Node getGrandparent() {
            if (parent != null) {
                return parent.parent;
            }

            return null;
        }

        public boolean isRightChild() {
            return parent.right == this;
        }

        // TODO replace with stored information
        public int size() {
            return 1 + ((left != null) ? left.size() : 0) + ((right != null) ? right.size() : 0);
        }

        public int rank() {
            return 1 + ((left != null) ? left.size() : 0);
        }

        @Override
        public String toString() {
            String out = "Node(" + value + ", " + rank();
            if (left != null) {
                out += "\n" + left.toString();
            }
            if (right != null) {
                out += "\n" + right.toString();
            }
            return out + ")";
        }
    }

    public Node insert(int value) {
        if (root == null) {
            root = new Node(value, null);
            fixTree(root);
            return root;
        }
        Node newNode = insertRecursive(root, value);
        fixTree(newNode);
        return newNode;
    }

    public Node insertRecursive(Node current, int value) {
        if (value <= current.value) {
            if (current.left == null) {
                current.left = new Node(value, current);
                return current.left;
            } else {
                return insertRecursive(current.left, value);
            }
            // value > current.value
        } else {
            if (current.right == null) {
                current.right = new Node(value, current);
                return current.right;
            } else {
                return insertRecursive(current.right, value);
            }
        }
    }

    public Node getByRank(int index) {
        return getByRank(root, index);
    }

    private Node getByRank(Node current, int value) {
        if (current == null) {
            return null;
        }
        int currentRank = current.rank();
        if (value < currentRank) {
            return getByRank(current.left, value);
        }
        if (value > currentRank) {
            return getByRank(current.right, value - currentRank);
        }

        return current;
    }

    public void fixTree(Node current) {
        Node parent = current.parent;
        Node gparent = current.getGrandparent();

        if (parent == null) {
            current.color = BLACK;
            return;
        }

        if (parent.color == BLACK) {
            return;
        }

        // Recolor
        if (current.getUncle() != null && current.getUncle().color == RED) {
            fixTreeRecolor(current);
        } else {
            // Rotation
            if (gparent.left == parent && parent.right == current) {
                rotateLeft(parent);
                current = current.left;
            } else if (gparent.right == parent && parent.left == current) {
                rotateRight(parent);
                current = current.right;
            }

            parent = current.parent;
            gparent = current.getGrandparent();

            if (current.parent.left == current) {
                rotateRight(gparent);
            } else if (current.parent.right == current) {
                rotateLeft(gparent);
            }

            parent.color = BLACK;

            if (gparent != root) {
                gparent.color = RED;
            }
        }
    }

    public void fixTreeRecolor(Node current) {
        current.parent.color = BLACK;
        current.getUncle().color = BLACK;
        current.getGrandparent().color = RED;
        fixTree(current.getGrandparent());
    }

    public void rotateLeft(Node current) {

        Node parent = current.parent;
        Node newChild = current.right;

        // Sets parent to currents right
        if (parent != null) {
            if (parent.left == current) {
                parent.left = newChild;

            } else if (parent.right == current) {
                parent.right = newChild;

            }
        }

        // Sets current right to the rights left
        current.right = newChild.left;

        if (current.right != null) {
            current.right.parent = current;

        }

        // Sets currents old rights left to current.
        newChild.left = current;

        // Set parent
        current.parent = newChild;
        newChild.parent = parent;

        if (newChild.parent == null) {
            root = newChild;
            root.color = BLACK;
        }
    }

    public void rotateRight(Node current) {
        Node parent = current.parent;
        Node newChild = current.left;

        if (parent != null) {
            if (parent.left == current) {
                parent.left = newChild;
            } else if (parent.right == current) {
                parent.right = newChild;
            }
        }

        current.left = newChild.right;

        if (current.left != null) {
            current.left.parent = current;
        }

        newChild.right = current;

        current.parent = newChild;
        newChild.parent = parent;

        if (newChild.parent == null) {
            root = newChild;
            root.color = BLACK;
        }
    }
}
