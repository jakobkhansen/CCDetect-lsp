package CCDetect.lsp;

import org.junit.Test;

import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.treesitter.TreeSitterLibrary;
import ai.serenade.treesitter.Languages;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.Tree;

public class TestGoParse {
    @Test
    public void testParse() throws Exception {
        // Configuration config = Configuration.getInstance();
        // config.setLanguage("go");
        Parser parser = TreeSitterLibrary.getParser();
        parser.setLanguage(Languages.go());
        Tree tree = parser.parseString("package main");
        System.out.println(tree.getRootNode().getNodeString());
    }
}
