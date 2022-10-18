package CCDetect.lsp.detection.treesitterbased;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ai.serenade.treesitter.Node;

public class TreesitterFingerprintGenerator {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    Map<String, Character> tokenToCharMap = new HashMap<>();
    char tokenCount = '$' + 1;

    // Generate the fingerprint of a single node, same token types will get same
    // char
    public String getFingerprint(Node node) {
        StringBuilder out = new StringBuilder();
        TokenFetchVisitor visitor = new TokenFetchVisitor();
        NodeTraversal.traverse(node, visitor);

        String[] tokens = visitor.getTokens();
        for (String token : tokens) {
            String fingerprint = String.valueOf(tokenToChar(token));
            out.append(fingerprint);
        }

        return out.toString();
    }

    private char tokenToChar(String token) {
        if (!tokenToCharMap.containsKey(token)) {
            tokenToCharMap.put(token, tokenCount++);
        }
        return tokenToCharMap.get(token);
    }
}
