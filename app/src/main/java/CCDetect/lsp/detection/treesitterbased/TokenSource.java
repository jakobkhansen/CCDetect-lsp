package CCDetect.lsp.detection.treesitterbased;

import ai.serenade.treesitter.TSRange;

/**
 * TokenSource
 */
public class TokenSource {

    String uri;
    TSRange range;

    public TokenSource(String uri, TSRange range) {
        this.uri = uri;
        this.range = range;
    }

    public String getUri() {
        return uri;
    }

    public TSRange getRange() {
        return range;
    }

}
