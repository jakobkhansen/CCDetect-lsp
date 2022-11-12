package CCDetect.lsp;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

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

        ProjectFileIterator iterator = new FiletypeIterator(rootUri, config.getLanguage());
        TreesitterDocumentIndex index = new TreesitterDocumentIndex(rootUri, iterator);
        index.indexProject();
    }
}
