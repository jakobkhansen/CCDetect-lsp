package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSRange;

public class TokenFetchVisitor implements NodeVisitor {

    List<String> types = new ArrayList<>();
    List<TSRange> ranges = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);
    NodeFilter filter = new NodeFilter();

    @Override
    public void visit(Node node) {
        if (filter.shouldFilter(node)) {
            return;
        }

        if (node.getChildCount() > 0) {
            return;
        }

        types.add(node.getType());
        ranges.add(new TSRange(node.getStartPoint(), node.getEndPoint(), node.getStartByte(), node.getEndByte()));
    }

    public String[] getTokens() {
        String[] out = new String[types.size()];
        return types.toArray(out);
    }

    public TSRange[] getRanges() {
        TSRange[] out = new TSRange[types.size()];
        return ranges.toArray(out);
    }
}
