package CCDetect.lsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import CCDetect.lsp.detection.treesitterbased.Fingerprint;
import CCDetect.lsp.detection.treesitterbased.TreesitterFingerprintGenerator;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.TSQueryCursor;
import ai.serenade.treesitter.TSQueryMatch;
import ai.serenade.treesitter.Tree;

/**
 * FingerprintGeneratorTest
 */
public class FingerprintGeneratorTest {
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
        config.setCloneTokenThreshold(75);
        config.setExtraNodes(new String[] {});
        config.setIgnoreNodes(new String[] {});
        config.setFragmentQuery("(method_declaration) @method");
    }

    @Test
    public void testFingerprintDirectly() {
        String[] input = new String[] {
                "public", "class", "Test", "{",
                "public", "void", "method", "(", ")", "{",
                "int", "x", "=", "3", ";",
                "}",
                "}"
        };
        int[] expected = new int[] { 2, 3, 4, 5, 2, 6, 7, 8, 9, 5, 10, 11, 12, 13, 14, 15, 15 };
        TreesitterFingerprintGenerator generator = new TreesitterFingerprintGenerator();

        for (int i = 0; i < input.length; i++) {
            String in = input[i];
            int exp = expected[i];
            assertEquals(exp, generator.tokenToValue(in));
        }

    }

    @Test
    public void testFingerprintNodeMethods() throws UnsupportedEncodingException {
        Parser parser = TreeSitterLibrary.getParser();
        String text = "public class Test { public void method() { int x = 3; }}";
        int[] expectedFingerprint = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 1 };
        TreesitterFingerprintGenerator generator = new TreesitterFingerprintGenerator();

        try (Tree tree = parser.parseString(text)) {
            Node root = tree.getRootNode();
            String query = config.getFragmentQuery();
            TSQueryCursor methodsQueryCursor = TreeSitterLibrary.queryPattern(root,
                    query);

            if (methodsQueryCursor == null) {
                return;
            }

            for (TSQueryMatch match = methodsQueryCursor.nextMatch(); match != null; match = methodsQueryCursor
                    .nextMatch()) {
                Node matchNode = match.getCaptures()[0].getNode();

                Fingerprint fingerprint = generator.getFingerprint(text, "", matchNode);
                Assert.assertArrayEquals(expectedFingerprint, fingerprint.getFingerprint());
            }
        }
    }
}
