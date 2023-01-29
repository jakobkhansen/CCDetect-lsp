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
import CCDetect.lsp.files.fileiterators.FiletypeIterator;
import CCDetect.lsp.files.fileiterators.ProjectFileIterator;
import CCDetect.lsp.server.Configuration;

/**
 * TestCodebaseCloneDetection
 */
public class TestCodebaseCloneDetection {
    Configuration config;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    @Before
    public void init() {
        // LOGGER.setFilter(new Filter() {
        // @Override
        // public boolean isLoggable(LogRecord record) {
        // return false;
        // }
        // });
        config = Configuration.getInstance();
        config.setLanguage("java");
        config.setCloneTokenThreshold(50);
        config.setExtraNodes(new String[] {});
        config.setIgnoreNodes(new String[] {});
        config.setFragmentQuery("(method_declaration) @method");
    }

    @Test
    public void testFindClonesInTestCodebase() {
        // String path = "src/test/resources/TestJavaCodebase";
        // File file = new File(path);
        // String rootUri = "file://" + file.getAbsolutePath();
        //
        // CodeClone[] expectedClones = new CodeClone[] {
        // new CodeClone(rootUri + "/Class.java", new Range(
        // new Position(11, 4),
        // new Position(17, 5))),
        // new CodeClone(rootUri + "/Another.java", new Range(
        // new Position(10, 4),
        // new Position(16, 5))),
        //
        // new CodeClone(rootUri + "/BinarySearch.java", new Range(
        // new Position(7, 35),
        // new Position(23, 5))),
        // new CodeClone(rootUri + "/BinarySearch.java", new Range(
        // new Position(30, 44),
        // new Position(46, 5))),
        //
        // new CodeClone(rootUri + "/LinkedList.java", new Range(
        // new Position(131, 25),
        // new Position(142, 9))),
        // new CodeClone(rootUri + "/LinkedList.java", new Range(
        // new Position(163, 19),
        // new Position(175, 5)))
        //
        // };
        //
        // ProjectFileIterator iterator = new FiletypeIterator(rootUri,
        // config.getLanguage());
        // TreesitterDocumentIndex index = new TreesitterDocumentIndex(rootUri,
        // iterator);
        // index.indexProject();
        // TreesitterDetector detector = new TreesitterDetector();
        // detector.onIndexChange(index);
        // List<CodeClone> clones = detector.getClones();
        //
        // for (CodeClone expectedClone : expectedClones) {
        // boolean found = false;
        // for (CodeClone realClone : clones) {
        // if (realClone.equals(expectedClone)) {
        // found = true;
        // break;
        // }
        // }
        // if (!found) {
        // fail("Expected clone not found: " + expectedClone);
        // }
        // }
        // assertEquals("Found incorrect number of clones", expectedClones.length,
        // clones.size());
    }

}
