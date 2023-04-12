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
import CCDetect.lsp.detection.treesitterbased.sourcemap.BinarySearchSourceMap;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.utils.Timer;

/**
 * IncrementalPerformanceEvaluation
 */
public class IncrementalPerformanceEvaluation {
    static DocumentIndex<TreesitterDocumentModel> index;
    static TreesitterDetector detector = new TreesitterDetector();
    static String originalRoot;

    public static void main(String[] args) throws Exception {
        Configuration.getInstance().setCloneTokenThreshold(100);
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

        Timer totalTimer = new Timer();
        totalTimer.start();

        Timer initialDetectionTimer = new Timer();
        initialDetectionTimer.start();

        originalRoot = "file://" + versionRoots[0].getPath();
        initialDetection();

        initialDetectionTimer.stop();
        initialDetectionTimer.logstdout("Initial detection time");

        for (int i = 1; i < versionRoots.length; i++) {
            Timer incrementalTimer = new Timer();
            incrementalTimer.start();
            incrementalUpdate(versionRoots[0].getPath(), versionRoots[i].getPath());
            incrementalTimer.stop();
            incrementalTimer.logstdout("Version " + i + " time");
        }

        totalTimer.stop();
        totalTimer.logstdout("Total running time");
    }

    public static void initialDetection() {
        System.out.println("Initial detection using root: " + originalRoot);
        index = new TreesitterDocumentIndex(originalRoot, new FiletypeIterator(originalRoot, "java"));
        index.indexProject();
        detector.onIndexChange(index);
    }

    public static void incrementalUpdate(String originalRoot, String root) throws Exception {
        System.out.println("Incremental update using root: " + root);
        originalRoot = "file://" + originalRoot;
        File changeFile = new File(root + "/changes");
        List<String> changedFiles = Files.readAllLines(Paths.get(changeFile.getPath()));
        for (String changedFile : changedFiles) {
            String operation = changedFile.split(" ")[0];
            switch (operation) {
                case "M":
                    System.out.println("Operation: M");
                    modifyFile(changedFile.split(" ")[1], root);
                    break;
                case "A":
                    System.out.println("Operation: A");
                    addFile(changedFile.split(" ")[1], root);
                    break;
                case "D":
                    System.out.println("Operation: D");
                    deleteFile(changedFile.split(" ")[1], root);
                    break;
            }

        }
        // detector.sourceMap = new BinarySearchSourceMap(index);
        detector.onIndexChange(index);
    }

    public static void modifyFile(String changedFilePath, String root) throws Exception {
        changedFilePath = changedFilePath.substring(1, changedFilePath.length() - 1);
        String originalPath = originalRoot + "/" + changedFilePath;
        changedFilePath = root + "/" + changedFilePath;
        String newContent = String.join("\n", Files.readAllLines(Paths.get(changedFilePath)));
        System.out.println(originalPath);
        index.updateDocument(originalPath, newContent);

    }

    public static void addFile(String changedFilePath, String root) throws Exception {
        changedFilePath = changedFilePath.substring(1, changedFilePath.length() - 1);
        String originalPath = originalRoot + "/" + changedFilePath;
        changedFilePath = root + "/" + changedFilePath;
        String newContent = String.join("\n", Files.readAllLines(Paths.get(changedFilePath)));
        System.out.println(originalPath);
        TreesitterDocumentModel model = new TreesitterDocumentModel(Paths.get(originalPath), newContent);
        model.setChanged(true);
        index.updateDocument(originalPath, model);

    }

    public static void deleteFile(String changedFilePath, String root) {
        changedFilePath = changedFilePath.substring(1, changedFilePath.length() - 1);
        String originalPath = originalRoot + "/" + changedFilePath;
        index.markFileDeleted(originalPath);
        index.deleteFile(originalPath);
    }
}
