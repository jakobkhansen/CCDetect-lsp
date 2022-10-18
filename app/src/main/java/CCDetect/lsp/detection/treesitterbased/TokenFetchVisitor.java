package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSRange;

public class TokenFetchVisitor implements NodeVisitor {

    List<String> tokens = new ArrayList<>();
    List<TSRange> ranges = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    @Override
    public void visit(Node node) {
        if (node.getChildCount() > 0) {
            return;
        }

        tokens.add(node.getType());
        ranges.add(new TSRange(node.getStartPoint(), node.getEndPoint(), node.getStartByte(), node.getEndByte()));
    }

    public String[] getTokens() {
        String[] out = new String[tokens.size()];
        return tokens.toArray(out);
    }

    public TSRange[] getRanges() {
        TSRange[] out = new TSRange[tokens.size()];
        return ranges.toArray(out);
    }
}
