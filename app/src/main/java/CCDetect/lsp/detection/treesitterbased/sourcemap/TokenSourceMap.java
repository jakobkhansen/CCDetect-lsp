package CCDetect.lsp.detection.treesitterbased.sourcemap;

import java.util.ArrayList;
import java.util.List;

import ai.serenade.treesitter.TSRange;

/**
 * TokenSourceMap
 */
public class TokenSourceMap implements SourceMap {

    List<TokenSource> sourceMap = new ArrayList<>();

    public void put(String uri, TSRange range) {
        sourceMap.add(new TokenSource(uri, range));
    }

    public TokenSource getSource(int index) {
        return sourceMap.get(index);
    }

    public int size() {
        return sourceMap.size();
    }
}
