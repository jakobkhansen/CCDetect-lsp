package CCDetect.lsp.detection.treesitterbased.nodetraversal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSRange;

public class TokenFetchVisitor implements NodeVisitor {

    List<TSRange> ranges = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);
    NodeFilter filter = new NodeFilter();

    @Override
    public void visit(Node node) {
        if (filter.shouldIgnore(node) || node.isExtra()) {
            return;
        }

        if (node.getChildCount() > 0 && !filter.isExtra(node)) {
            return;
        }

        ranges.add(node.toRange());
    }

    public TSRange[] getRanges() {
        TSRange[] out = new TSRange[ranges.size() + 1];
        return ranges.toArray(out);
    }
}
