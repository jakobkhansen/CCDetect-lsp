package CCDetect.lsp.detection.treesitterbased.fingerprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import CCDetect.lsp.detection.treesitterbased.NodeTraversal;
import CCDetect.lsp.detection.treesitterbased.TokenFetchVisitor;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSRange;

public class TreesitterFingerprintGenerator {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    Map<String, Integer> tokenToCharMap = new HashMap<>();
    int tokenCounter = 2;
    int tokensSeen = 0;

    // Generate the fingerprint of a single node, same token types will get same
    // char
    public Fingerprint getFingerprint(String text, String uri, Node node) {
        List<Integer> out = new ArrayList<>();
        TokenFetchVisitor visitor = new TokenFetchVisitor();
        NodeTraversal.traverse(node, visitor);

        TSRange[] ranges = visitor.getRanges();
        for (TSRange range : ranges) {
            if (range == null) {
                continue;
            }
            String token = text.substring(range.getStartByte(), range.getEndByte());
            out.add(tokenToValue(token));
            tokensSeen++;
            if (tokensSeen % 1000000 == 0) {
                LOGGER.info("Processed " + tokensSeen + " tokens");
            }
        }

        // Method delimiter
        out.add(1);

        return new Fingerprint(Ints.toArray(out), ranges, uri, node.toRange());
    }

    public int tokenToValue(String token) {
        if (!tokenToCharMap.containsKey(token)) {
            tokenToCharMap.put(token, tokenCounter++);
        }
        return tokenToCharMap.get(token);
    }

    public Map<String, Integer> getTokenToCharMap() {
        return tokenToCharMap;
    }
}
