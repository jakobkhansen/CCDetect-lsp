package CCDetect.lsp.utils;

import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import ai.serenade.treesitter.TSPoint;
import ai.serenade.treesitter.TSRange;

/**
 * RangeConverter
 */
public class RangeConverter
        extends Converter<Range, TSRange> {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public RangeConverter() {
        super(
                RangeConverter::convertToTSRange,
                RangeConverter::convertToLSPRange);
    }

    private static Range convertToLSPRange(TSRange range) {
        TSPoint startPoint = range.getStartPoint();
        TSPoint endPoint = range.getEndPoint();
        Position start = new Position(startPoint.row, startPoint.column);
        Position end = new Position(endPoint.row, endPoint.column);
        return new Range(start, end);
    }

    private static TSRange convertToTSRange(
            Range range) {
        return new TSRange(null, null, 0, 0);
    }
}
