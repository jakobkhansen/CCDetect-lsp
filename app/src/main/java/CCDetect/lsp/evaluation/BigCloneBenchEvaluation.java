package CCDetect.lsp.evaluation;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.utils.Printer;

/**
 * BigCloneBenchEvaluation
 */
public class BigCloneBenchEvaluation {

    static FileWriter output;
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);
    static HashSet<CodeClone> visited = new HashSet<>();

    public static void main(String[] args) throws Exception {
        LOGGER.setLevel(Level.OFF);
        // System.out.println("Evaluting CCDetect with Big Clone Bench...");

        output = new FileWriter("evaluation.txt");
        Configuration.getInstance().setCloneTokenThreshold(100);
        Configuration.getInstance().setDynamicDetection(false);
        Configuration.getInstance()
                .setFragmentQuery("(method_declaration) @method (constructor_declaration) @constructor");
        String root = System.getProperty("bcbPath");

        DocumentIndex<TreesitterDocumentModel> index = new TreesitterDocumentIndex(root,
                new FiletypeIterator(root, "java"));
        index.indexProject();
        TreesitterDetector detector = new TreesitterDetector();
        detector.onIndexChange(index);

        for (CodeClone clone : detector.getClones()) {
            addCloneToOutput(clone);
        }

        output.flush();
        output.close();
    }

    public static void addCloneToOutput(CodeClone clone) throws Exception {
        String subfolder = clone.getUri().split("/")[clone.getUri().split("/").length - 2];
        String filename = clone.getUri().split("/")[clone.getUri().split("/").length - 1];
        String start = (clone.getRange().getStart().getLine() + 1) + "";
        String end = (clone.getRange().getEnd().getLine() + 1) + "";

        for (CodeClone match : clone.getMatches()) {
            String matchSubfolder = match.getUri().split("/")[match.getUri().split("/").length - 2];
            String matchFilename = match.getUri().split("/")[match.getUri().split("/").length - 1];
            String matchStart = (match.getRange().getStart().getLine() + 1) + "";
            String matchEnd = (match.getRange().getEnd().getLine() + 1) + "";
            String outputLine = String.join(",", subfolder, filename, start, end,
                    matchSubfolder, matchFilename,
                    matchStart,
                    matchEnd);
            output.write(outputLine + "\n");
        }

        // HashSet<CodeClone> visited = new HashSet<>();
        // Queue<CodeClone> queue = new LinkedList<>();
        // visited.add(clone);
        // for (CodeClone match : clone.getMatches()) {
        // if (!visited.contains(match)) {
        // visited.add(match);
        // queue.add(match);
        // }
        // }
        // while (!queue.isEmpty()) {
        // CodeClone currentClone = queue.remove();
        // String matchSubfolder =
        // currentClone.getUri().split("/")[currentClone.getUri().split("/").length -
        // 2];
        // String matchFilename =
        // currentClone.getUri().split("/")[currentClone.getUri().split("/").length -
        // 1];
        // String matchStart = (currentClone.getRange().getStart().getLine() + 1) + "";
        // String matchEnd = (currentClone.getRange().getEnd().getLine() + 1) + "";
        // String outputLine = String.join(",", subfolder, filename, start, end,
        // matchSubfolder, matchFilename,
        // matchStart,
        // matchEnd);
        // output.write(outputLine + "\n");
        // for (CodeClone match : currentClone.getMatches()) {
        // if (!visited.contains(match)) {
        // visited.add(match);
        // queue.add(match);
        // }
        // }
        //
        // }
    }
}
