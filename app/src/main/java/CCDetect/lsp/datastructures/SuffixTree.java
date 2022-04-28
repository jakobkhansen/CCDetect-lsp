package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SuffixTree
 */
// Could have generics, will do later, don't speculate on generality
public class SuffixTree {

    private static final Logger LOGGER = Logger.getLogger(
        Logger.GLOBAL_LOGGER_NAME
    );

    class Node {

        public int position;
        public String text;
        public List<Node> children = new ArrayList<>();
        public int debugId;
        public String path = "";

        public Node(String text, int position, int debugId) {
            this.text = text;
            this.position = position;
            this.debugId = debugId;
        }
    }

    public class Match {

        public String text;
        public int length;
        public List<Integer> positions;

        public Match(String text, List<Integer> positions) {
            this.text = text;
            this.length = text.length();
            this.positions = positions;
        }
    }

    String input;
    public Node root;
    public int debugId;

    public SuffixTree(String input) {
        this.input = input;
        root = new Node("", -1, debugId++);
        for (int i = 0; i < input.length(); i++) {
            LOGGER.info("Adding suffix " + input.substring(i));
            addSuffix(input.substring(i), i);
        }
    }

    private void addChildNode(Node parentNode, String suffix, int position) {
        parentNode.children.add(new Node(suffix, position, debugId++));
    }

    private String getLongestCommonPrefix(String suffix1, String suffix2) {
        int compareLength = Math.min(suffix1.length(), suffix2.length());

        for (int i = 0; i < compareLength; i++) {
            if (suffix1.charAt(i) != suffix2.charAt(i)) {
                return suffix1.substring(0, i);
            }
        }

        return suffix1.substring(0, compareLength);
    }

    private void splitNode(
        Node parentNode,
        String parentNewSuffix,
        String childNewSuffix
    ) {
        Node childNode = new Node(
            childNewSuffix,
            parentNode.position,
            debugId++
        );

        // Move all children from parent to child
        while (!parentNode.children.isEmpty()) {
            childNode.children.add(
                parentNode.children.remove(parentNode.children.size() - 1)
            );
        }

        parentNode.children.add(childNode);
        parentNode.text = parentNewSuffix;
        parentNode.position = -1;
    }

    private List<Node> nodesInTraversePath(
        String pattern,
        Node startNode,
        boolean partialMatch
    ) {
        List<Node> nodes = new ArrayList<>();
        for (Node currentNode : startNode.children) {
            // System.out.println("Looking at node with prefix " + currentNode.suffix);
            String currentText = currentNode.text;
            if (currentText.charAt(0) == pattern.charAt(0)) {
                // System.out.println("Found partial match");
                if (partialMatch && pattern.length() <= currentText.length()) {
                    nodes.add(currentNode);
                    return nodes;
                }

                int compareLength = Math.min(
                    currentText.length(),
                    pattern.length()
                );
                for (int j = 1; j < compareLength; j++) {
                    if (pattern.charAt(j) != currentText.charAt(j)) {
                        if (partialMatch) {
                            nodes.add(currentNode);
                        }
                        return nodes;
                    }
                }

                nodes.add(currentNode);

                if (pattern.length() > compareLength) {
                    List<Node> recurNodes = nodesInTraversePath(
                        pattern.substring(compareLength),
                        currentNode,
                        partialMatch
                    );

                    if (recurNodes.size() > 0) {
                        nodes.addAll(recurNodes);
                    }
                }
                return nodes;
            }
        }

        return nodes;
    }

    private void addSuffix(String suffix, int position) {
        List<Node> nodes = nodesInTraversePath(suffix, root, true);
        for (Node node : nodes) {
            System.out.println("Node in path: " + node.text);
        }
        if (nodes.size() == 0) {
            addChildNode(root, suffix, position);
        } else {
            Node lastNode = nodes.remove(nodes.size() - 1);
            String newText = suffix;
            if (nodes.size() > 0) {
                String existingSuffix = nodes
                    .stream()
                    .map(a -> a.text)
                    .reduce("", String::concat);

                newText = newText.substring(existingSuffix.length());
            }

            extendNode(lastNode, newText, position);
        }
    }

    private void extendNode(Node node, String newSuffix, int position) {
        String currentPrefix = node.text;
        String commonPrefix = getLongestCommonPrefix(currentPrefix, newSuffix);

        if (commonPrefix.length() != currentPrefix.length()) {
            String parentPrefix = currentPrefix.substring(
                0,
                commonPrefix.length()
            );
            String childPrefix = currentPrefix.substring(commonPrefix.length());
            splitNode(node, parentPrefix, childPrefix);
        }

        String remainingList = newSuffix.substring(commonPrefix.length());
        addChildNode(node, remainingList, position);
    }

    private List<Node> getInternalNodes(Node start, String path) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(start);
        start.path = path + start.text;

        for (Node child : start.children) {
            if (!child.children.isEmpty()) {
                nodes.addAll(getInternalNodes(child, start.path));
            } else {
                child.path = path + child.text;
            }
        }

        return nodes;
    }

    public List<Match> getMatches(int lengthThreshold) {
        List<Match> matches = new ArrayList<>();

        for (Node start : root.children) {
            List<Node> internals = getInternalNodes(start, "");
            for (Node internal : internals) {
                int length = internal.path.length();
                if (length >= lengthThreshold) {
                    List<Integer> matchPositions = new ArrayList<>();

                    List<Node> nodeQueue = new ArrayList<>();
                    nodeQueue.addAll(internal.children);

                    while (!nodeQueue.isEmpty()) {
                        Node child = nodeQueue.remove(nodeQueue.size()-1);
                        if (child.position != -1) {
                            matchPositions.add(child.position);
                        } else {
                            nodeQueue.addAll(child.children);
                        }
                    }
                    matches.add(new Match(internal.path, matchPositions));
                }
            }
        }

        return matches;
    }

    public static void main(String[] args) {
        String input = "ABCDEF#LBCDE#NBCDE#$";
        SuffixTree tree = new SuffixTree(input);

        System.out.println("hello");
        List<Match> matches = tree.getMatches(4);
        for (Match match : matches) {
            System.out.println("Match(" + match.text + ", " + match.length + ")");
            for (int pos : match.positions) {
                System.out.println("pos " + pos);
            }
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("SuffixTree\n");
        out.append("Root:\n");
        out.append(preorderToString(root));

        return out.toString();
    }

    public String preorderToString(Node current) {
        StringBuilder out = new StringBuilder();
        out.append(
            "Node at position " +
            current.position +
            ", with suffix: " +
            current.text +
            ", debug: " +
            current.debugId +
            "\n"
        );
        out.append("Children:\n");

        for (Node child : current.children) {
            out.append(
                "Position: " +
                current.position +
                ", Suffix: " +
                child.text +
                ", debug: " +
                child.debugId +
                "\n"
            );
        }

        out.append("\n");

        for (Node child : current.children) {
            out.append(preorderToString(child));
        }

        return out.toString();
    }
}
