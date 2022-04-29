package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SuffixTree
 */
// Could have generics, will do later, don't speculate on generality
public class SuffixTree {

    public class Node {

        public int position;
        public String text;
        public List<Node> children = new ArrayList<>();
        public String path = "";

        public Node(String text, int position) {
            this.text = text;
            this.position = position;
        }
    }


    String input;
    public Node root;
    public int debugId;

    public SuffixTree(String input) {
        this.input = input;
        root = new Node("", -1);
        for (int i = 0; i < input.length(); i++) {
            addSuffix(input.substring(i), i);
        }
    }

    private void addChildNode(Node parentNode, String suffix, int position) {
        parentNode.children.add(new Node(suffix, position));
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
            parentNode.position
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
            String currentText = currentNode.text;
            if (currentText.charAt(0) == pattern.charAt(0)) {
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

    public List<Node> getInternalNodes(Node start, String path) {
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
