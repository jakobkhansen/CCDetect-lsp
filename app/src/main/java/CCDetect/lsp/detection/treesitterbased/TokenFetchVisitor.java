package CCDetect.lsp.detection.treesitterbased;

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
        LOGGER.info("NODE: " + node.getNodeString());
        LOGGER.info("Range: " + Printer.print(node.toRange()));
        if (filter.shouldFilter(node) || node.isExtra()) {
            LOGGER.info("Filtered " + node.getType());
            return;
        }

        if (node.getChildCount() > 0) {
            return;
        }

        ranges.add(node.toRange());
    }

    public TSRange[] getRanges() {
        TSRange[] out = new TSRange[ranges.size() + 1];
        return ranges.toArray(out);
    }
}
