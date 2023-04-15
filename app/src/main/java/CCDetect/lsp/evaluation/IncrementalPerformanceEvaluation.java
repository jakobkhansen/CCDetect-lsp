package CCDetect.lsp.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    static String outputFile;
    static List<Double> timingsMS = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Configuration.getInstance().setCloneTokenThreshold(100);
        // Configuration.getInstance().setLazyLCPUpdates(true);
        String root = System.getProperty("root");
        outputFile = System.getProperty("outputFile");
        String mode = System.getProperty("mode");

        if (mode.equals("saca")) {
            Configuration.getInstance().setDynamicDetection(false);
            System.out.println("Running SACA performance evaluation at root: " + root);
        } else {

            System.out.println("Running incremental performance evaluation at root: " + root);
        }

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
        timingsMS.add(initialDetectionTimer.getTotal());

        for (int i = 1; i < versionRoots.length; i++) {
            Timer incrementalTimer = new Timer();
            incrementalTimer.start();
            incrementalUpdate(versionRoots[0].getPath(), versionRoots[i].getPath());
            incrementalTimer.stop();
            incrementalTimer.logstdout("Version " + i + " time");
            timingsMS.add(incrementalTimer.getTotal());
        }

        totalTimer.stop();
        totalTimer.logstdout("Total running time");

        writeToOutput();
    }

    public static void initialDetection() {
        System.out.println("Initial detection using root: " + originalRoot);
        index = new TreesitterDocumentIndex(originalRoot, new FiletypeIterator(originalRoot, "java"));
        index.indexProject();
        detector.onIndexChange(index);
    }

    public static void incrementalUpdate(String originalRoot, String root) throws Exception {
        originalRoot = "file://" + originalRoot;
        File changeFile = new File(root + "/changes");
        List<String> changedFiles = Files.readAllLines(Paths.get(changeFile.getPath()));
        for (String changedFile : changedFiles) {
            String operation = changedFile.split(" ")[0];
            switch (operation) {
                case "M":
                    modifyFile(changedFile.split(" ")[1], root);
                    break;
                case "A":
                    addFile(changedFile.split(" ")[1], root);
                    break;
                case "D":
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

    public static void writeToOutput() throws Exception {
        File file = new File(outputFile);
        FileWriter writer = new FileWriter(outputFile);
        String output = Stream.of(timingsMS).map((x) -> ("" + x)).collect(Collectors.joining(","));
        writer.write(output);
        writer.close();
    }
}
