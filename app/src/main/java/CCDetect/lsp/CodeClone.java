package CCDetect.lsp;

import org.eclipse.lsp4j.Range;

/**
 * CodeClone
 */
public class CodeClone {

    private String uri;
    private Range range;
    private CodeClone pairMatch;

    public CodeClone(String uri, Range range) {
        this.uri = uri;
        this.range = range;
    }

    public static void setMatch(CodeClone clone1, CodeClone clone2) {
        clone1.setMatchingClone(clone2);
        clone2.setMatchingClone(clone1);
    }

    public boolean isInRange(Range otherRange) {
        // Check if start below start
        int currentStartLine = range.getStart().getLine();
        int otherStartLine = otherRange.getStart().getLine();

        boolean belowStart = currentStartLine <= otherStartLine;

        // Check if end is above end
        int currentEndLine = range.getEnd().getLine();
        int otherEndLine = otherRange.getEnd().getLine();

        boolean aboveEnd = currentEndLine >= otherEndLine;

        // Check if below start and above end
        return belowStart && aboveEnd;
    }

    public CodeClone getPairMatch() {
        return pairMatch;
    }

    public Range getRange() {
        return range;
    }

    public String getUri() {
        return uri;
    }

    public void setMatchingClone(CodeClone clone) {
        this.pairMatch = clone;
    }

    @Override
    public String toString() {
        String out = "CodeClone(" + this.uri + range + ")";
        return out;
    }
}
