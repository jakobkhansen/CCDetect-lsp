package CCDetect.lsp.detection.tokenbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import CCDetect.lsp.files.DocumentLine;
import CCDetect.lsp.files.DocumentModel;

/**
 * LimeEngine
 */

/*
   Current plan:
   Have a list of integers which maps to tokens
   Build suffix tree from this
   Need a table from index in integer list back to token I guess

   However, turns out the algorithm actually just fingerprints entire lines
   of code for each function, I need to extract all functions and get all the code then?

   Currently, I'm extracting each line (removing white space and such), not just methods. 
   Plan is to take all lines, fingerprint them (to integer) and put them in a suffix tree. From
*/
public class LimeEngine {
    // Table of all tokens per file
    HashMap<DocumentModel, List<DocumentLine>> linesInDocument = new HashMap<>();

    // Table from source-code line to an integer, for use in fingerprint
    HashMap<String, Integer> lineToIntegerMapping = new HashMap<>();

    // Map from index in fingerprint list back to line
    HashMap<Integer, DocumentLine> indexToLineMapping = new HashMap<>();


    // Fingerprint for all files/methods/... (granularity)
    List<Integer> fingerprint = new ArrayList<>();

    int lineFingerprintCounter = 0;


    public void addDocumentLines(DocumentModel document, List<DocumentLine> lines) {
        linesInDocument.put(document, lines);
        for (DocumentLine line : lines) {
            if (!lineToIntegerMapping.containsKey(line.toString())) {
                lineToIntegerMapping.put(line.toString(), lineFingerprintCounter);
                lineFingerprintCounter++;
            }
            int lineFingerprint = lineToIntegerMapping.get(line.toString());
            int index = fingerprint.size();
            fingerprint.add(lineFingerprint);
            indexToLineMapping.put(index, line);
        }
        fingerprint.add(-1);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        // Fingerprint
        result.append("Fingerprint: [ ");
        for (int i : fingerprint) {
            result.append(i + " ");
        }
        result.append("]\n\n");

        result.append("Integer mapping:\n");
        // Fingerprint per line
        for (String key : lineToIntegerMapping.keySet()) {
            result.append(key + ": " + lineToIntegerMapping.get(key) + "\n");
        }

        return result.toString();
    }
}
