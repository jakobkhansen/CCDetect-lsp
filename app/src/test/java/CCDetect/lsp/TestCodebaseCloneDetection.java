package CCDetect.lsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.Before;
import org.junit.Test;

import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.files.fileiterators.ProjectFileIterator;
import CCDetect.lsp.server.Configuration;

/**
 * TestCodebaseCloneDetection
 */
public class TestCodebaseCloneDetection {
    Configuration config;

    @Before
    public void init() {
        config = Configuration.getInstance();
        config.setLanguage("java");
        config.setCloneTokenThreshold(50);
        config.setExtraNodes(new String[] {});
        config.setIgnoreNodes(new String[] {});
        config.setFragmentQuery("(method_declaration) @method");
    }

    @Test
    public void testFindClonesInTestCodebase() {
        String path = "src/test/resources/TestJavaCodebase";
        File file = new File(path);
        String rootUri = "file://" + file.getAbsolutePath();

        CodeClone[] expectedClones = new CodeClone[] {
                new CodeClone(rootUri + "/Class.java", new Range(
                        new Position(10, 4),
                        new Position(16, 5))),

                new CodeClone(rootUri + "/Another.java", new Range(
                        new Position(9, 4),
                        new Position(15, 5)))
        };

        ProjectFileIterator iterator = new FiletypeIterator(rootUri, config.getLanguage());
        TreesitterDocumentIndex index = new TreesitterDocumentIndex(rootUri, iterator);
        index.indexProject();
        TreesitterDetector detector = new TreesitterDetector();
        detector.onIndexChange(index);
        List<CodeClone> clones = detector.getClones();

        for (CodeClone expectedClone : expectedClones) {
            boolean found = false;
            for (CodeClone realClone : clones) {
                if (realClone.equals(expectedClone)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Expected clone not found");
            }
        }
    }

}
