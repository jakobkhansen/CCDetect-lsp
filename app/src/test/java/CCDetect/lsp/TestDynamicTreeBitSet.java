package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import CCDetect.lsp.datastructures.rankselect.DynamicBitSet;
import CCDetect.lsp.datastructures.rankselect.DynamicTreeBitSet;
import CCDetect.lsp.utils.Printer;

/**
 * TestDynamicTreeBitSet
 */
public class TestDynamicTreeBitSet {

    @Test
    public void testBuildingTree() {
        int size = 33;
        DynamicTreeBitSet tree = new DynamicTreeBitSet(size);
        tree.set(0, true);
        tree.set(3, true);
        tree.set(18, true);
        assertEquals(true, tree.get(0));
        assertEquals(true, tree.get(3));
        assertEquals(true, tree.get(18));
        assertEquals(false, tree.get(1));

        assertEquals(false, tree.get(2));
        assertEquals(false, tree.get(17));
        assertEquals(false, tree.get(19));
        assertEquals(false, tree.get(32));

        tree.set(32, true);
        assertEquals(true, tree.get(32));
    }

    @Test
    public void testRanking() {
        int size = 33;
        DynamicTreeBitSet tree = new DynamicTreeBitSet(size);
        tree.set(0, true);
        tree.set(3, true);
        tree.set(18, true);
        assertEquals(2, tree.rank(18, true));
        tree.set(3, false);
        assertEquals(1, tree.rank(18, true));

        tree.set(32, true);
        assertEquals(true, tree.get(32));
    }

    @Test
    public void TestTreeInsert() {
        DynamicTreeBitSet set = new DynamicTreeBitSet(32);
        set.set(1, true);
        set.set(2, true);
        set.set(3, true);
        set.set(9, true);
        set.insert(2, false);
        assertEquals(false, set.get(0));
        assertEquals(true, set.get(1));
        assertEquals(false, set.get(2));
        assertEquals(true, set.get(3));
        assertEquals(true, set.get(4));
        assertEquals(false, set.get(5));

        set.insert(20, true);
        assertEquals(true, set.get(20));
    }

    @Test
    public void testTreeDelete() {
        DynamicTreeBitSet set = new DynamicTreeBitSet(32);
        set.set(1, true);
        set.set(2, true);
        set.set(3, true);
        set.set(9, true);
        set.delete(2);
        assertEquals(false, set.get(0));
        assertEquals(true, set.get(1));
        assertEquals(true, set.get(2));
        assertEquals(false, set.get(3));
        assertEquals(false, set.get(4));
        assertEquals(false, set.get(5));
        assertEquals(true, set.get(8));
        assertEquals(false, set.get(9));

        set.set(20, true);
        set.set(21, true);
        set.delete(20);
        assertEquals(true, set.get(20));
        assertEquals(false, set.get(21));
    }
}
