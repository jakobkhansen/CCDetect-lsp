package CCDetect.lsp.detection.treesitterbased;

import ai.serenade.treesitter.Node;

public class NodeTraversal {
    public static void traverse(Node node, NodeVisitor visitor) {
        visitor.visit(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            traverse(node.getChild(i), visitor);
        }
    }
}
