package CCDetect.lsp.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SuffixTree
 */
// Could have generics, will do later, don't speculate on generality
public class SuffixTree {

    class Node {

        public int position;
        public String suffix;
        public List<Node> children = new ArrayList<>();

        public Node(String suffix, int position) {
            this.suffix = suffix;
            this.position = position;
        }
    }

    public class Match {
        public int length;
        public List<Integer> positions;

        public Match(int length, List<Integer> positions) {
            this.length = length;
            this.positions = positions;
        }

    }

    String input;
    public Node root;

    public SuffixTree(String input) {
        this.input = input;
        root = new Node("", -1);
        for (int i = input.length()-1; i >= 0; i--) {
            addSuffix(input.substring(i), i);
        }
    }

    private void addChildNode(
        Node parentNode,
        String suffix,
        int position
    ) {
        parentNode.children.add(new Node(suffix, position));
    }

    private String getLongestCommonPrefix(
        String suffix1,
        String suffix2
    ) {
        int compareLength = Math.min(suffix1.length(), suffix2.length());
        List<Integer> prefix = new ArrayList<>();

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
        Node childNode = new Node(childNewSuffix, parentNode.position);

        // Move all children from parent to child
        while (!parentNode.children.isEmpty()) {
            childNode.children.add(
                parentNode.children.remove(parentNode.children.size() - 1)
            );
        }

        parentNode.children.add(childNode);
        parentNode.suffix = parentNewSuffix;
    }

    private List<Node> nodesInTraversePath(
        String pattern,
        Node startNode,
        boolean partialMatch
    ) {
        List<Node> nodes = new ArrayList<>();
        for (Node currentNode : startNode.children) {
            // System.out.println("Looking at node with prefix " + currentNode.suffix);
            String currentText = currentNode.suffix;
            if (currentText.charAt(0) == pattern.charAt(0)) {
                // System.out.println("Found partial match");
                if (partialMatch && pattern.length() <= currentText.length()) {
                    nodes.add(currentNode);
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
        // System.out.println("Adding suffix " + suffix);
        // System.out.println("Nodes in path " + nodes.size());
        if (nodes.size() == 0) {
            addChildNode(root, suffix, position);
        } else {
            Node lastNode = nodes.remove(nodes.size()-1);
            String existingSuffix = nodes.stream()
                .map(a -> a.suffix)
                .reduce("", String::concat);
            String newSuffix = suffix.substring(existingSuffix.length());

            extendNode(lastNode, newSuffix, position);
        }
    }

    private void extendNode(Node node, String newSuffix, int position) {

        String currentPrefix = node.suffix;
        String commonPrefix = getLongestCommonPrefix(currentPrefix, newSuffix);

        if (commonPrefix.length() != currentPrefix.length()) {
            String parentPrefix = currentPrefix.substring(
                0,
                commonPrefix.length()
            );
            String childPrefix = currentPrefix.substring(
                commonPrefix.length()
            );
            splitNode(node, parentPrefix, childPrefix);
        }

        String remainingList = newSuffix.substring(commonPrefix.length());
        addChildNode(node, remainingList, position);
    }

    private List<Node> getInternalNodes(Node start, int suffixThreshold) {
        List<Node> nodes = new ArrayList<>();

        for (Node child : start.children) {
            if (!child.children.isEmpty()) {
                if (child.suffix.length() >= suffixThreshold) {
                    nodes.add(child);
                }
                nodes.addAll(getInternalNodes(child, suffixThreshold));
            }
        }

        return nodes;
    }

    public List<Match> getMatches(int lengthThreshold) {
        List<Match> matches = new ArrayList<>();

        List<Node> internals = getInternalNodes(root, lengthThreshold);

        for (Node internal : internals) {
            int length = internal.suffix.length();
            List<Integer> matchPositions = new ArrayList<>();
            for (Node child : internal.children) {
                matchPositions.add(child.position);
            }
            matches.add(new Match(length, matchPositions));
        }

        return matches;
    }

    public static void main(String[] args) {
        String input = "0123456AA$012784569AA#";
        SuffixTree tree = new SuffixTree(input);
        
        System.out.println(tree);
        for (Node internal : tree.getInternalNodes(tree.root, 3)) {
            System.out.println(internal.position + " " + internal.suffix);
            for (Node child : internal.children) {
                System.out.println(child.position);
            }
            System.out.println();
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
        out.append("Node at position " + current.position + ", with suffix: " + current.suffix + "\n");
        out.append("Children:\n");

        for (Node child : current.children) {
            out.append("Position: " + current.position + ", Suffix: " + child.suffix + "\n");
        }

        out.append("\n");

        for (Node child : current.children) {
            out.append(preorderToString(child));
        }

        return out.toString();
    }

}
