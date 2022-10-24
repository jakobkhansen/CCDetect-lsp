package CCDetect.lsp.detection.treesitterbased;

import ai.serenade.treesitter.TSPoint;
import ai.serenade.treesitter.TSRange;

/**
 * TokenSourcePair
 */
public class TokenSourcePair {

    TokenSource left;
    TokenSource right;

    public TokenSourcePair(TokenSource left, TokenSource right) {
        this.left = left;
        this.right = right;
    }

    public TSRange getRangeBetween() {
        if (left.getRange() == null || right.getRange() == null) {
            return null;
        }
        int leftStartByte = left.getRange().getStartByte();
        int rightEndByte = right.getRange().getEndByte();

        TSPoint leftStart = left.getRange().getStartPoint();
        TSPoint rightEnd = right.getRange().getEndPoint();

        TSRange range = new TSRange(leftStart, rightEnd, leftStartByte, rightEndByte);

        return range;
    }

    public String getUri() {
        return right.getUri();
    }

}
