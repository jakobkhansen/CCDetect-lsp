package CCDetect.lsp.detection.tokenbased;

import org.eclipse.lsp4j.Range;

/**
 * Token
 */
public class Token {

    private String token;
    private String uri;
    private Range range;

    public Token(String token, String uri, Range range) {
        super();
        this.token = token;
        this.uri = uri;
        this.range = range;
    }
}
