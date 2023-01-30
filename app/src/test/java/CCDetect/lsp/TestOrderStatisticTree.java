package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import CCDetect.lsp.datastructures.DynamicPermutation;
import CCDetect.lsp.datastructures.OrderStatisticTree;

/**
 * TestOrderStatisticTree
 */
public class TestOrderStatisticTree {

    @Test
    public void testOrderStatisticTree() {
        OrderStatisticTree tree = new OrderStatisticTree();
        for (int i = 0; i < 100; i++) {
            tree.add(i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
        tree.add(20);
        for (int i = 0; i < 101; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
        tree.add(50);
        for (int i = 0; i < 102; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
        tree.add(75);
        for (int i = 0; i < 103; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
        tree.add(76);
        for (int i = 0; i < 103; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
    }

    @Test
    public void testOrderStatisticTreeDelete() {

        int[] initial = { 6, 5, 0, 2, 4, 1, 3 };
        int[] first = { 7, 6, 0, 3, 5, 2, 1, 4, };
        int[] second = { 6, 5, 0, 2, 4, 1, 3 };
        OrderStatisticTree tree = new OrderStatisticTree();
        for (int i : initial) {
            tree.add(i);
        }
        for (int i = 0; i < initial.length; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
        tree.add(2);
        for (int i = 0; i < first.length; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }
        tree.remove(2);
        for (int i = 0; i < second.length; i++) {
            assertEquals(i, DynamicPermutation.inOrderRank(tree.getByRank(i)));
        }

    }
}
