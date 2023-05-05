package CCDetect.lsp;

import org.junit.Test;

import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.Tree;

public class TestPythonParse {
    @Test
    public void testParse() throws Exception {
        Configuration config = Configuration.getInstance();
        config.setLanguage("py");
        Parser parser = TreeSitterLibrary.getParser();
        Tree tree = parser.parseString("print('hello world')");
        System.out.println(tree.getRootNode().getNodeString());
    }
}
