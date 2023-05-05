package CCDetect.lsp;

import org.junit.Test;

import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Languages;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.Tree;

public class TestRustParse {
    @Test
    public void testParse() throws Exception {
        Configuration config = Configuration.getInstance();
        Parser parser = new Parser();
        parser.setLanguage(Languages.rust());
        Tree tree = parser.parseString("fn main() { println!(\"Hello World!\"); }");
        System.out.println(tree.getRootNode().getNodeString());
        parser.close();
    }
}
