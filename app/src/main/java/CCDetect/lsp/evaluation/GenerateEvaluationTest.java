package CCDetect.lsp.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;

import CCDetect.lsp.detection.treesitterbased.nodetraversal.NodeFilter;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.TSQueryCursor;
import ai.serenade.treesitter.TSQueryMatch;

/**
 * GenerateEvaluationTest
 */
public class GenerateEvaluationTest {

    static String root = System.getProperty("root");
    static String fileType = System.getProperty("fileType");
    static int changes = Integer.parseInt(System.getProperty("changes"));
    static int versions = Integer.parseInt(System.getProperty("versions"));
    static int minSize = Integer.parseInt(System.getProperty("minSize"));
    static int maxSize = Integer.parseInt(System.getProperty("maxSize"));
    static String query = "(method_declaration) @method (constructor_declaration) @constructor";
    static Set<Path> usedPaths = new HashSet<>();

    // Quick hack to allow an increasing amount of changes
    static int DELTA_CHANGES = 5;

    public static void main(String[] args) throws Exception {
        Configuration.getInstance()
                .setIgnoreNodes(
                        new String[] { "comment", "import_declaration", "package_declaration", "scoped_identifier" });
        String root = System.getProperty("root");
        System.out.println(root);
        System.out.println("fileType: " + fileType);
        System.out.println("versions: " + versions);
        System.out.println("changes: " + changes);

        for (int v = 1; v < versions; v++) {
            copySourceCode(v);
        }

        for (int v = 1; v < versions; v++) {
            System.out.println("Changes in version " + v + ": " + changes);
            createVersion(v);
            changes = (changes + DELTA_CHANGES) - (changes % DELTA_CHANGES);
        }
    }

    public static void copySourceCode(int version) throws Exception {
        File source = new File(root + "/a");
        File destination = new File(root + "/" + ((char) ('a' + version)));
        FileUtils.copyDirectory(source, destination);
    }

    public static void createVersion(int version) throws Exception {
        String previousRoot = root + "/a";
        String currentRoot = root + "/" + ((char) ('a' + version));
        FiletypeIterator fileIter = new FiletypeIterator("file://" + previousRoot, fileType);
        NodeFilter filter = new NodeFilter();
        List<Path> paths = StreamSupport.stream(fileIter.spliterator(), false).collect(Collectors.toList());
        Collections.shuffle(paths);

        int numGenerated = 0;

        for (Path p : paths) {
            if (usedPaths.contains(p)) {
                continue;
            }
            TreesitterDocumentModel model = new TreesitterDocumentModel(p, null);
            model.buildTree();
            Node rootNode = model.getAST().getTree().getRootNode();
            TSQueryCursor methodsQueryCursor = TreeSitterLibrary.queryPattern(rootNode,
                    query);
            for (TSQueryMatch match = methodsQueryCursor.nextMatch(); match != null; match = methodsQueryCursor
                    .nextMatch()) {
                Node matchNode = match.getCaptures()[0].getNode();

                if (filter.shouldIgnore(matchNode) || matchNode.isExtra()) {
                    continue;
                }

                if (filterRecursiveChildren(matchNode)) {
                    continue;
                }

                int numTokens = numberOfTokensInNode(matchNode);
                if (numTokens >= minSize && numTokens <= maxSize) {
                    for (int i = version; i < versions; i++) {
                        String versionRoot = root + "/" + ((char) ('a' + i));
                        File newFile = new File(p.toString().replace(previousRoot, versionRoot));
                        generateDeletion(i, model, newFile, matchNode);
                    }
                    numGenerated++;
                    usedPaths.add(p);
                    break;
                }
            }
            if (numGenerated >= changes) {
                break;
            }
        }
    }

    private static boolean filterRecursiveChildren(Node node) {

        Node current = node.getParent();
        while (current != null) {
            if (current.getType().equals(node.getType())) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }

    private static int numberOfTokensInNode(Node node) {
        if (node.getChildCount() == 0) {
            return 1;
        }

        int numberOfTokens = 0;

        for (int i = 0; i < node.getChildCount(); i++) {
            numberOfTokens += numberOfTokensInNode(node.getChild(i));
        }

        return numberOfTokens;
    }

    public static void generateDeletion(int version, TreesitterDocumentModel originalFile, File newFile,
            Node node)
            throws Exception {
        String text = originalFile.getText();
        String prefix = text.substring(0, node.getStartByte());
        String suffix = text.substring(node.getEndByte());
        String newText = prefix + suffix;
        newFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter(newFile);
        fw.write(newText);
        fw.close();

    }
}
