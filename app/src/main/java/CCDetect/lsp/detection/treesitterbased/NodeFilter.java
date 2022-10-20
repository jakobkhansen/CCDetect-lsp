package CCDetect.lsp.detection.treesitterbased;

import java.util.HashMap;
import java.util.Map;

import CCDetect.lsp.server.Configuration;
import ai.serenade.treesitter.Node;

public class NodeFilter {
    Map<String, Boolean> nodesToFilter = new HashMap<>();

    public NodeFilter() {
        Configuration config = Configuration.getInstance();
        for (String node : config.getIgnoreNodes()) {
            nodesToFilter.put(node, true);
        }
    }

    public boolean shouldFilter(String node) {
        return nodesToFilter.containsKey(node);
    }

    public boolean shouldFilter(Node node) {
        return shouldFilter(node.getType());
    }
}
