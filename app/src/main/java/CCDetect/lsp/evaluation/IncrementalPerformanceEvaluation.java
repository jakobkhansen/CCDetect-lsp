package CCDetect.lsp.evaluation;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.server.Configuration;

/**
 * IncrementalPerformanceEvaluation
 */
public class IncrementalPerformanceEvaluation {
    static DocumentIndex<TreesitterDocumentModel> index;
    static TreesitterDetector detector = new TreesitterDetector();

    public static void main(String[] args) throws Exception {
        Configuration.getInstance().setCloneTokenThreshold(75);
        String root = System.getProperty("root");
        System.out.println("Running performance evaluation at root: " + root);

        File rootDir = new File(new URI(root));
        File[] versionRoots = rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                File f = new File(current, name);
                return f.isDirectory();
            }
        });

        Arrays.sort(versionRoots);
        initialDetection("file://" + versionRoots[0].getPath());
        for (int i = 1; i < versionRoots.length; i++) {
            incrementalUpdate(versionRoots[0].getPath(), versionRoots[i].getPath());
        }
    }

    public static void initialDetection(String root) {
        System.out.println("Initial detection using root: " + root);
        index = new TreesitterDocumentIndex(root, new FiletypeIterator(root, "java"));
        index.indexProject();
        detector.onIndexChange(index);
    }

    public static void incrementalUpdate(String originalRoot, String root) throws Exception {
        System.out.println("Incremental update using root: " + root);
        originalRoot = "file://" + originalRoot;
        File changeFile = new File(root + "/changes");
        List<String> changedFiles = Files.readAllLines(Paths.get(changeFile.getPath()));
        for (String changedFile : changedFiles) {
            String changedFilePath = changedFile.split(" ")[1];
            changedFilePath = changedFilePath.substring(1, changedFilePath.length() - 1);
            String originalPath = originalRoot + "/" + changedFilePath;
            changedFilePath = root + "/" + changedFilePath;
            String newContent = String.join("\n", Files.readAllLines(Paths.get(changedFilePath)));
            System.out.println(originalPath);
            index.updateDocument(originalPath, newContent);

        }
        detector.onIndexChange(index);

    }
}
