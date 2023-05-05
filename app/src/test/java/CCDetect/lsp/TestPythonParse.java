package CCDetect.lsp;

import org.junit.Test;

import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Languages;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.Tree;

public class TestPythonParse {
    @Test
    public void testParse() throws Exception {
        Parser parser = new Parser();
        parser.setLanguage(Languages.python());
        Tree tree = parser.parseString("print('hello world')");
        System.out.println(tree.getRootNode().getNodeString());
    }
}
