package CCDetect.lsp.detection.tokenbased;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.datastructures.SuffixTree;
import CCDetect.lsp.datastructures.SuffixTree.Match;
import CCDetect.lsp.files.DocumentLine;
import CCDetect.lsp.files.DocumentModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

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

    private static final Logger LOGGER = Logger.getLogger(
        Logger.GLOBAL_LOGGER_NAME
    );

    int LINE_MATCH_THRESHOLD = 4;
    // Table of all methods per file
    HashMap<DocumentModel, List<DocumentMethod>> methodsInDocument = new HashMap<>();

    // Table from source-code line to an integer, for use in fingerprint
    HashMap<String, Character> lineToIntegerMapping = new HashMap<>();

    // Map from index in fingerprint list back to line
    HashMap<Integer, DocumentLine> indexToLineMapping = new HashMap<>();

    // Fingerprint for all files/methods/... (granularity)
    StringBuilder fingerprint = new StringBuilder();

    int lineFingerprintCounter = 65;

    public void addMethod(DocumentModel document, DocumentMethod method) {
        List<DocumentMethod> methods = methodsInDocument.getOrDefault(
            document,
            new ArrayList<>()
        );
        methods.add(method);
        methodsInDocument.put(document, methods);

        for (DocumentLine line : method.getLines()) {
            if (!lineToIntegerMapping.containsKey(line.toString())) {
                lineToIntegerMapping.put(
                    line.toString(),
                    (char) lineFingerprintCounter
                );
                lineFingerprintCounter++;
            }
            char lineFingerprint = lineToIntegerMapping.get(line.toString());
            int index = fingerprint.length();
            fingerprint.append(lineFingerprint);
            indexToLineMapping.put(index, line);
        }
        fingerprint.append("#");
    }

    public List<CodeClone> match() {
        fingerprint.append("$");
        List<CodeClone> clones = new ArrayList<>();
        SuffixTree tree = new SuffixTree(fingerprint.toString());

        List<Match> matches = tree.getMatches(LINE_MATCH_THRESHOLD);

        for (Match match : matches) {
            List<CodeClone> cloneMatches = new ArrayList<>();
            for (int pos : match.positions) {

                List<DocumentLine> lines = new ArrayList<>();
                for (int i = pos; i < pos + match.length; i++) {
                    if (fingerprint.charAt(i) != '#') {

                        lines.add(indexToLineMapping.get(i));
                    }
                }
                DocumentLine firstLine = lines.get(0);
                DocumentLine lastLine = lines.get(lines.size() - 1);
                Range range = new Range(
                    new Position(firstLine.line - 1, 0),
                    new Position(lastLine.line - 1, lastLine.text.length())
                );

                cloneMatches.add(new CodeClone(firstLine.uri, range));
            }
            for (int i = 0; i < cloneMatches.size(); i++) {
                for (int j = i + 1; j < cloneMatches.size(); j++) {
                    CodeClone.addMatch(cloneMatches.get(i), cloneMatches.get(j));
                }
            }
            clones.addAll(cloneMatches);
        }

        LOGGER.info("Num clones: " + clones.size());
        return clones;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        // Fingerprint
        result.append("Fingerprint: [ " + fingerprint.toString() + " ]\n\n");

        result.append("Integer mapping:\n");
        // Fingerprint per line
        for (String key : lineToIntegerMapping.keySet()) {
            result.append(key + ": " + lineToIntegerMapping.get(key) + "\n");
        }

        return result.toString();
    }
}
