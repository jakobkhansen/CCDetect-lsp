package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import CCDetect.lsp.datastructures.rankselect.DynamicBitSet;
import CCDetect.lsp.utils.Printer;

/**
 * TestDynamicBitSet
 */
public class TestDynamicBitSet {

    @Test
    public void TestDynBitSet() {
        DynamicBitSet set = new DynamicBitSet(10, 100);
        set.set(1);
        set.set(2);
        set.set(3);
        set.set(9);
        assertEquals(false, set.get(4));
        assertEquals(true, set.get(9));
        assertEquals(true, set.get(1));
    }

    @Test
    public void TestDynBitSetInsert() {
        DynamicBitSet set = new DynamicBitSet(10, 100);
        set.set(1);
        set.set(2);
        set.set(3);
        set.set(9);
        set.insert(2, false);
        assertEquals(false, set.get(2));
        assertEquals(true, set.get(4));
    }
}
