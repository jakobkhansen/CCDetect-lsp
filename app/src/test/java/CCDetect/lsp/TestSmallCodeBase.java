package CCDetect.lsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.Before;
import org.junit.Test;

import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.files.fileiterators.ProjectFileIterator;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.utils.Printer;

/**
 * TestCodebaseCloneDetection
 */
public class TestSmallCodeBase {
    Configuration config;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    @Before
    public void init() {
        LOGGER.setFilter(new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                return false;
            }
        });
        config = Configuration.getInstance();
        config.setLanguage("java");
        config.setCloneTokenThreshold(10);
        config.setExtraNodes(new String[] {});
        config.setIgnoreNodes(new String[] {});
        config.setFragmentQuery("(method_declaration) @method");
        config.setDynamicDetection(true);
    }

    @Test
    public void testFindClonesInSmallTestCodebase() {
        String path = "src/test/resources/TestSmallCodeBase";
        File file = new File(path);
        String rootUri = "file://" + file.getAbsolutePath();

        CodeClone[] expectedClones = new CodeClone[] {
                new CodeClone(rootUri + "/HelloWorld.java", new Range(
                        new Position(7, 21),
                        new Position(10, 5))),
                new CodeClone(rootUri + "/HelloWorld.java", new Range(
                        new Position(12, 23),
                        new Position(15, 5)))
        };

        ProjectFileIterator iterator = new FiletypeIterator(rootUri,
                config.getLanguage());
        TreesitterDocumentIndex index = new TreesitterDocumentIndex(rootUri,
                iterator);
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
                fail("Expected clone not found: " + expectedClone);
            }
        }
        assertEquals("Found incorrect number of clones", expectedClones.length,
                clones.size());
    }

}
