package CCDetect.lsp.detection.treesitterbased.nodetraversal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import CCDetect.lsp.utils.Printer;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSRange;

public class TokenFetchVisitor implements NodeVisitor {

    List<Node> nodes = new ArrayList<>();
    NodeFilter filter = new NodeFilter();

    @Override
    public void visit(Node node) {
        if (node.getChildCount() > 0 && !filter.isExtra(node)) {
            return;
        }

        if (filter.shouldIgnore(node) || node.isExtra()) {
            return;
        }

        nodes.add(node);
    }

    public Node[] getNodes() {
        Node[] out = new Node[nodes.size() + 1];
        return nodes.toArray(out);
    }

    public TSRange[] getRanges() {
        TSRange[] out = new TSRange[nodes.size() + 1];
        List<TSRange> ranges = nodes.stream().map((x) -> x.toRange()).collect(Collectors.toList());
        return ranges.toArray(out);
    }
}
