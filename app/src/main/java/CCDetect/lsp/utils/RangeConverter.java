package CCDetect.lsp.utils;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * RangeConverter
 */
public class RangeConverter
        extends Converter<Range, com.github.javaparser.Range> {

    public RangeConverter() {
        super(
                RangeConverter::convertToParserRange,
                RangeConverter::convertToLSPRange);
    }

    private static Range convertToLSPRange(com.github.javaparser.Range range) {
        Position start = new Position(range.begin.line - 1, range.begin.column - 1);
        Position end = new Position(range.end.line - 1, range.end.column);
        return new Range(start, end);
    }

    private static com.github.javaparser.Range convertToParserRange(
            Range range) {
        com.github.javaparser.Position start = new com.github.javaparser.Position(
                range.getStart().getLine(),
                range.getStart().getCharacter());
        com.github.javaparser.Position end = new com.github.javaparser.Position(
                range.getEnd().getLine(),
                range.getEnd().getCharacter());
        return new com.github.javaparser.Range(start, end);
    }
}
