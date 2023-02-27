package CCDetect.lsp.evaluation;

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

    public static void main(String[] args) {
        System.out.println("Evaluting CCDetect with Big Clone Bench...");
        Configuration.getInstance().setCloneTokenThreshold(100);
        String root = System.getProperty("bcbPath");
        DocumentIndex<TreesitterDocumentModel> index = new TreesitterDocumentIndex(root,
                new FiletypeIterator(root, "java"));
        index.indexProject();
        TreesitterDetector detector = new TreesitterDetector();
        detector.onIndexChange(index);
        System.out.println("Got " + detector.getClones().size() + " clones");
        System.out.println("Sample first clone and its matches");
        System.out.println(detector.getClones().get(0));
        System.out.println("Matches:");
        for (CodeClone c : detector.getClones().get(0).getMatches()) {
            System.out.println(c);
        }
    }
}
