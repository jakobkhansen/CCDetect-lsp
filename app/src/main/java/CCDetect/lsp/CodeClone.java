package CCDetect.lsp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Range;

/**
 * CodeClone
 */
public class CodeClone {

    private String uri;
    private Range range;
    private List<CodeClone> matchingClones = new ArrayList<>();

    public CodeClone(String uri, Range range) {
        this.uri = uri;
        this.range = range;
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

    public void addMatchingClone(CodeClone clone) {
        matchingClones.add(clone);
    }

    public List<CodeClone> getMatches() {
        return matchingClones;
    }

    public Range getRange() {
        return range;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        String out = "CodeClone(" + this.uri + range + "\nMatches: " + this.matchingClones.size() + ")";
        return out;
    }

    public static void addMatch(CodeClone clone1, CodeClone clone2) {
        clone1.addMatchingClone(clone2);
        clone2.addMatchingClone(clone1);
    }

    public boolean equals(CodeClone clone) {
        return range.equals(clone.getRange()) && uri.equals(clone.getUri());
    }
}
