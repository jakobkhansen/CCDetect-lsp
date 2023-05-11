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
        Parser parser = new Parser();
        parser.setLanguage(Languages.go());
        Tree tree = parser.parseString("package main");
        System.out.println(tree.getRootNode().getNodeString());
        parser.close();
    }
}
