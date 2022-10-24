package CCDetect.lsp.utils;

import java.util.Map;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.detection.treesitterbased.Fingerprint;
import CCDetect.lsp.detection.treesitterbased.TokenSource;
import CCDetect.lsp.detection.treesitterbased.TreesitterFingerprintGenerator;
import ai.serenade.treesitter.TSInputEdit;
import ai.serenade.treesitter.TSPoint;
import ai.serenade.treesitter.TSRange;

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
        String out = "TSInputEdit(";
        out += "\nstart_byte: " + edit.startByte;
        out += "\nold_end_byte: " + edit.oldEndByte;
        out += "\nnew_end_byte: " + edit.newEndByte;
        out += "\nstart_point: " + print(edit.start_point);
        out += "\nold_end_point: " + print(edit.old_end_point);
        out += "\nnew_end_point: " + print(edit.new_end_point);

        return out + ")";
    }

    public static String print(TSPoint point) {
        String out = "TSPoint(";
        out += point.row + ", " + point.column + ")";

        return out;
    }

    public static String print(TSRange range) {
        if (range == null) {
            return "TSRange(null)";
        }
        String out = "TSRange(";
        out += "\nstart_byte: " + range.getStartByte();
        out += "\nend_byte: " + range.getEndByte();
        out += "\nstart_point: " + print(range.getStartPoint());
        out += "\nend_point: " + print(range.getEndPoint());

        return out + ")";
    }

    public static String print(String[] strings) {
        String out = "String[ ";
        for (String s : strings) {
            out += s + " ";
        }
        return out + "]";
    }

    public static String print(int[] ints) {
        StringBuilder out = new StringBuilder("int[ ");
        for (int s : ints) {
            out.append(s + " ");
        }
        out.append("]");
        return out.toString();
    }

    public static <K, V> String print(Map<K, V> map) {
        StringBuilder out = new StringBuilder("Map(");
        for (K key : map.keySet()) {
            V value = map.get(key);
            out.append("\n" + key.toString() + ": " + value.toString());
        }

        out.append("\n");
        return out.toString();
    }

    public static String print(Fingerprint fingerprint) {
        String out = "Fingerprint(\n";

        out += fingerprint.getUri() + "\n";
        out += fingerprint.getFingerprint() + "\n";
        out += print(fingerprint.getMethodRange()) + "\n";

        return out;
    }

    public static String print(TreesitterFingerprintGenerator fingerprintGenerator) {
        String out = "FingerprintGeneratorMap(\n";
        out += print(fingerprintGenerator.getTokenToCharMap());
        return out + ")";
    }

    public static String print(TokenSource tokenSource) {
        StringBuilder out = new StringBuilder("TokenSource(\n");
        out.append("uri: " + tokenSource.getUri() + "\n");
        out.append(print(tokenSource.getRange()) + "\n");

        return out.toString();
    }
}
