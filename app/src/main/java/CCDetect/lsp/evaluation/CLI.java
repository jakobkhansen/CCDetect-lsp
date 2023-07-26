package CCDetect.lsp.evaluation;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.server.Configuration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CLI {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {
        LOGGER.setLevel(Level.OFF);

        System.out.println("Running CCDetect-LSP in CLI mode...");
        Configuration.getInstance().setLanguage("java");
        Configuration.getInstance().setCloneTokenThreshold(100);
        Configuration.getInstance().setDynamicDetection(false);
        Configuration
            .getInstance()
            .setFragmentQuery("(method_declaration) @method (constructor_declaration) @constructor");

        // Type-2 tree-sitter node types here
        Configuration
            .getInstance()
            .setBlindNodes(
                new String[] {
                    "name",
                    "identifier",
                    "string_literal",
                    "decimal_integer_literal",
                    "decimal_floating_point_literal",
                    "type_identifier",
                }
            );
        String root = System.getProperty("root");

        DocumentIndex<TreesitterDocumentModel> index = new TreesitterDocumentIndex(
            root,
            new FiletypeIterator(root, "java")
        );

        index.indexProject();
        TreesitterDetector detector = new TreesitterDetector();
        detector.onIndexChange(index);

        // Clones
        for (CodeClone clone : detector.getClones()) {
            System.out.println(clone + "\n");
        }
    }
}
