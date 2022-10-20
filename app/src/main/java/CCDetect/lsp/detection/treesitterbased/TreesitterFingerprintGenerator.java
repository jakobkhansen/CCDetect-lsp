package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.primitives.Ints;

import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSRange;

public class TreesitterFingerprintGenerator {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    Map<String, Integer> tokenToCharMap = new HashMap<>();
    int tokenCount = 1;

    // Generate the fingerprint of a single node, same token types will get same
    // char
    public int[] getFingerprint(String text, Node node) {
        List<Integer> out = new ArrayList<>();
        TokenFetchVisitor visitor = new TokenFetchVisitor();
        NodeTraversal.traverse(node, visitor);

        TSRange[] ranges = visitor.getRanges();
        for (TSRange range : ranges) {
            String token = text.substring(range.getStartByte(), range.getEndByte());
            out.add(tokenToValue(token));
        }

        return Ints.toArray(out);
    }

    private int tokenToValue(String token) {
        if (!tokenToCharMap.containsKey(token)) {
            tokenToCharMap.put(token, tokenCount++);
        }
        return tokenToCharMap.get(token);
    }

    public Map<String, Integer> getTokenToCharMap() {
        return tokenToCharMap;
    }
}