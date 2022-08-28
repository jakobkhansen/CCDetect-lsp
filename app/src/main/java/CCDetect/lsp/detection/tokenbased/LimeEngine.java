package CCDetect.lsp.detection.tokenbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.datastructures.SuffixTree;
import CCDetect.lsp.datastructures.SuffixTree.Node;
import CCDetect.lsp.files.DocumentLine;
import CCDetect.lsp.files.DocumentModel;

/**
 * LimeEngine
 */

/*
 * Current plan:
 * Have a list of integers which maps to tokens
 * Build suffix tree from this
 * Need a table from index in integer list back to token I guess
 * 
 * However, turns out the algorithm actually just fingerprints entire lines
 * of code for each function, I need to extract all functions and get all the
 * code then?
 */
public class LimeEngine {

    public class Match {

        public String text;
        public int length;
        public List<Integer> positions;

        public Match(String text, List<Integer> positions) {
            this.text = text;
            this.length = text.length();
            this.positions = positions;
        }

        @Override
        public String toString() {
            String out = "[ ";
            for (int pos : positions) {
                out += pos + " ";
            }
            return out + "]";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

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
                new ArrayList<>());
        methods.add(method);
        methodsInDocument.put(document, methods);

        for (DocumentLine line : method.getLines()) {
            if (!lineToIntegerMapping.containsKey(line.toString())) {
                lineToIntegerMapping.put(
                        line.toString(),
                        (char) lineFingerprintCounter);
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

        List<Match> matches = getMatches(tree, LINE_MATCH_THRESHOLD);
        matches = filterOverlappingMatches(matches);

        for (Match match : matches) {
            LOGGER.info(match.toString());
        }

        for (Match match : matches) {
            List<CodeClone> cloneMatches = new ArrayList<>();
            for (int pos : match.positions) {
                List<DocumentLine> lines = new ArrayList<>();
                for (int i = pos; i < pos + match.length; i++) {
                    if (fingerprint.charAt(i) != '#') {
                        lines.add(indexToLineMapping.get(i));
                    } else {
                        break;
                    }
                }
                DocumentLine firstLine = lines.get(0);
                DocumentLine lastLine = lines.get(lines.size() - 1);
                Range range = new Range(
                        new Position(firstLine.line - 1, 0),
                        new Position(lastLine.line - 1, 100));

                cloneMatches.add(new CodeClone(firstLine.uri, range));
            }
            for (int i = 0; i < cloneMatches.size(); i++) {
                for (int j = i + 1; j < cloneMatches.size(); j++) {
                    CodeClone.addMatch(
                            cloneMatches.get(i),
                            cloneMatches.get(j));
                }
            }
            clones.addAll(cloneMatches);
        }

        // clones.add(new
        // CodeClone("file:///home/jakob/Documents/CompilaServerTest/test01.ccdetect",
        // new Range(new Position(0,0), new Position(2, 3))));
        LOGGER.info("Num clones: " + clones.size());
        return clones;
    }

    private List<Match> getMatches(SuffixTree tree, int lengthThreshold) {
        List<Match> matches = new ArrayList<>();

        for (Node start : tree.root.children) {
            List<Node> internals = tree.getInternalNodes(start, "");
            for (Node internal : internals) {
                int length = internal.path.length();

                if (length >= lengthThreshold) {
                    HashSet<Integer> matchPositions = new HashSet<>();

                    List<Node> nodeQueue = new ArrayList<>();
                    nodeQueue.addAll(internal.children);

                    while (!nodeQueue.isEmpty()) {
                        Node child = nodeQueue.remove(nodeQueue.size() - 1);
                        if (child.position != -1) {
                            matchPositions.add(child.position);
                        } else {
                            nodeQueue.addAll(child.children);
                        }
                    }
                    matches.add(
                            new Match(
                                    internal.path,
                                    matchPositions.stream().collect(Collectors.toList())));
                }
            }
        }

        return matches;
    }

    // Compare matches, if a match location completely overlaps another, remove the
    // smallest
    public List<Match> filterOverlappingMatches(List<Match> matches) {
        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                Match m1 = matches.get(i);
                Match m2 = matches.get(j);

                removeOverlappingPositions(m1, m2);
            }
        }

        return matches;
    }

    // Compare positions in a match, if one completely overlaps another, remove the
    // smallest
    public void removeOverlappingPositions(Match m1, Match m2) {
        for (int i = 0; i < m1.positions.size(); i++) {
            for (int j = 0; j < m2.positions.size(); j++) {
                int pos1 = m1.positions.get(i);
                int pos2 = m2.positions.get(j);

                int pos1End = pos1 + m1.length;
                int pos2End = pos2 + m2.length;

                if (m1.length >= m2.length) {
                    if (pos1 <= pos2 && pos2End <= pos1End) {
                        m2.positions.remove(j);
                        j--;
                    }
                } else {
                    if (pos2 <= pos1 && pos1End <= pos2End) {
                        m1.positions.remove(i);
                        i--;
                    }
                }
            }
        }
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
