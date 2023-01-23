package CCDetect.lsp.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;
import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.detection.treesitterbased.fingerprint.TreesitterFingerprintGenerator;
import CCDetect.lsp.detection.treesitterbased.sourcemap.TokenSource;
import CCDetect.lsp.detection.treesitterbased.sourcemap.TokenSourceMap;
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
        StringBuilder out = new StringBuilder("int[ ");
        for (String s : strings) {
            out.append(Strings.padEnd("" + s, 6, ' ') + " ");
        }
        out.append("]");
        return out.toString();
    }

    public static String print(int[] ints) {
        StringBuilder out = new StringBuilder("int[ ");
        for (int s : ints) {
            out.append(Strings.padEnd("" + s, 6, ' ') + " ");
        }
        out.append("]");
        return out.toString();
    }

    public static String print(int[] ints, int size) {
        StringBuilder out = new StringBuilder("int[ ");
        for (int i = 0; i < size; i++) {
            out.append(Strings.padEnd("" + ints[i], 6, ' ') + " ");
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

    public static String print(TokenSourceMap sourceMap) {
        StringBuilder out = new StringBuilder("TokenSourceMap(\n");
        for (int i = 0; i < sourceMap.size(); i++) {
            out.append(i + ": " + Printer.print(sourceMap.getSource(i)));
        }

        return out.toString();
    }

    public static String print(EditOperation edit) {
        StringBuilder out = new StringBuilder("EditOperation(\n");
        out.append("position: " + edit.getPosition() + "\n");
        out.append("Type: " + edit.getOperationType() + "\n");
        out.append("Chars: ");
        for (int i : edit.getChars()) {
            out.append(i + " ");
        }
        out.append("\n");

        return out.toString();
    }

    public static String print(BitSet set) {
        StringBuilder out = new StringBuilder("BitSet( ");
        for (int i = 0; i < set.size(); i++) {
            int val = (set.get(i)) ? 1 : 0;
            out.append(val + " ");
        }
        out.append(")");

        return out.toString();
    }

    public static String print(BitSet set, int size) {
        StringBuilder out = new StringBuilder("BitSet( ");
        for (int i = 0; i < size; i++) {
            int val = (set.get(i)) ? 1 : 0;
            out.append(val + " ");
        }
        out.append(")");

        return out.toString();
    }

    public static String print(WaveletMatrix wm) {
        StringBuilder out = new StringBuilder("WaveletMatrix( \n");
        for (int i = 0; i < wm.getMatrix().length; i++) {
            out.append(print(wm.getMatrix()[i], wm.getInputSize()));
            out.append("\n");
        }
        out.append(")");

        return out.toString();
    }
}
