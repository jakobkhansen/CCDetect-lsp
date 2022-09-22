package CCDetect.lsp.utils;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.TSPoint;

public class Printer {
    public static String print(Range range) {
        Position startPos = range.getStart();
        Position endPos = range.getEnd();

        int startLine = startPos.getLine();
        int endLine = endPos.getLine();

        int startChar = startPos.getCharacter();
        int endChar = endPos.getCharacter();

        String out = "Range((" + startLine + ", " + startChar + ")";
        out += ", (" + endLine + ", " + endChar + "))";
        return out;
    }

    public static String print(TSInputEdit edit) {
        String out = "TSInputEdit(\n";
        out += "\nstart_byte: " + edit.startByte;
        out += "\nold_end_byte: " + edit.oldEndByte;
        out += "\nnew_end_byte: " + edit.newEndByte;
        out += "\nstart_point" + print(edit.start_point);
        out += "\nold_end_point" + print(edit.old_end_point);
        out += "\nnew_end_point" + print(edit.new_end_point);

        return out;
    }

    public static String print(TSPoint point) {
        String out = "TSPoint(";
        out += point.row + ", " + point.column + ")";

        return out;
    }
}
