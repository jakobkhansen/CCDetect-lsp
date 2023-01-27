package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import CCDetect.lsp.datastructures.RedBlackTree;
import CCDetect.lsp.datastructures.RedBlackTree.Node;

/**
 * TestRedBlackTree
 */
public class TestRedBlackTree {

    @Test
    public void testRedBlack() {
        RedBlackTree tree = new RedBlackTree();
        int[] insertValues = { 10, 20, 30, 25, 23 };
        for (int val : insertValues) {
            tree.insert(val);
        }
    }
}
