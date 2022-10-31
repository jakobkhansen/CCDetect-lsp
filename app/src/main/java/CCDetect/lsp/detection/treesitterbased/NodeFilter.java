package CCDetect.lsp.detection.treesitterbased;

import java.util.HashMap;
import java.util.Map;

import CCDetect.lsp.server.Configuration;
import ai.serenade.treesitter.Node;

public class NodeFilter {
    Map<String, Boolean> nodesToFilter = new HashMap<>();
    Map<String, Boolean> extraNodes = new HashMap<>();

    public NodeFilter() {
        Configuration config = Configuration.getInstance();
        for (String node : config.getIgnoreNodes()) {
            nodesToFilter.put(node, true);
        }
        for (String node : config.getExtraNodes()) {
            extraNodes.put(node, true);
        }
    }

    public boolean shouldIgnore(String node) {
        return nodesToFilter.containsKey(node);
    }

    public boolean shouldIgnore(Node node) {
        return shouldIgnore(node.getType());
    }

    public boolean isExtra(String node) {
        return extraNodes.containsKey(node);
    }

    public boolean isExtra(Node node) {
        return isExtra(node.getType());
    }
}
