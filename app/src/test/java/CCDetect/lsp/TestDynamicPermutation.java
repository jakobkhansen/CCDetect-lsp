package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import CCDetect.lsp.datastructures.DynamicPermutation;
import CCDetect.lsp.utils.Printer;

/**
 * TestDynamicPermutation
 */
public class TestDynamicPermutation {

    @Test
    public void testDynamicPermutationSmall() {
        int[] input = { 0, 1, 2, 3 };
        DynamicPermutation perm = new DynamicPermutation(input, new int[] { 0, 0, 0, 0 });
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        int[] newInput = { 0, 2, 1, 3, 4 };
        perm.insert(1, 2, 0);
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
    }

    @Test
    public void testDynamicPermutation() {
        int[] input = { 3, 0, 5, 1, 4, 2 };
        DynamicPermutation perm = new DynamicPermutation(input, new int[] { 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        int[] newInput = { 4, 2, 0, 6, 1, 5, 3 };
        perm.insert(1, 2, 0);
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
        int[] finalInput = { 0, 5, 3, 1, 7, 2, 6, 4 };
        perm.insert(0, 0, 0);

        for (int i = 0; i < finalInput.length; i++) {
            assertEquals(finalInput[i], perm.get(i));
        }
    }

    @Test
    public void testDeleteInDynamicPermutation() {
        int[] input = { 3, 0, 5, 1, 4, 2 };
        DynamicPermutation perm = new DynamicPermutation(input, new int[] { 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        perm.delete(0);
        int[] newInput = { 0, 4, 1, 3, 2 };
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
        perm.delete(4);
        int[] finalInput = { 0, 3, 1, 2 };
        for (int i = 0; i < finalInput.length; i++) {
            assertEquals(finalInput[i], perm.get(i));
        }
    }

    @Test
    public void testSuffixAndInverse() {
        int[] sa = { 3, 0, 5, 1, 4, 2 };
        int[] isa = { 1, 3, 5, 0, 4, 2 };
        DynamicPermutation perm = new DynamicPermutation(sa, new int[] { 0, 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < sa.length; i++) {
            assertEquals(sa[i], perm.get(i));
            assertEquals(isa[i], perm.getInverse(i));
        }
    }

    @Test
    public void testDeleteAndInsert() {
        int[] first = { 3, 0, 5, 1, 4, 2 };
        int[] newInput = { 4, 2, 0, 6, 1, 5, 3 };
        DynamicPermutation perm = new DynamicPermutation(first, new int[] { 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], perm.get(i));
        }
        perm.insert(1, 2, 0);
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
        perm.delete(1);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], perm.get(i));
        }
        perm.insert(1, 2, 0);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
    }

    @Test
    public void testEdgecase() {
        int[] initial = { 6, 5, 0, 2, 4, 1, 3 };
        int[] first = { 7, 6, 0, 3, 5, 2, 1, 4, };
        int[] second = { 6, 5, 0, 2, 4, 1, 3 };

        DynamicPermutation perm = new DynamicPermutation(initial, new int[] { 0, 0, 0, 0, 0, 0, 0 });

        for (int i = 0; i < initial.length; i++) {
            assertEquals(initial[i], perm.get(i));
        }
        perm.insert(5, 2, 0);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], perm.get(i));
        }
        perm.delete(6);
        for (int i = 0; i < second.length; i++) {
            assertEquals(second[i], perm.get(i));
        }

    }

    @Test
    public void testEdgecaseTwo() {
        int[] first = { 7, 6, 0, 3, 5, 2, 1, 4, };

        int[] second = { 6, 5, 0, 2, 4, 1, 3 };
        DynamicPermutation perm = new DynamicPermutation(first, new int[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], perm.get(i));
        }
        perm.delete(6);
        for (int i = 0; i < second.length; i++) {
            assertEquals(second[i], perm.get(i));
        }

    }
}
