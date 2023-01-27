package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import CCDetect.lsp.datastructures.DynamicPermutation;

/**
 * TestDynamicPermutation
 */
public class TestDynamicPermutation {

    @Test
    public void testDynamicPermutation() {
        int[] input = { 3, 0, 5, 1, 4, 2 };
        DynamicPermutation perm = new DynamicPermutation(input);
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        int[] newInput = { 4, 2, 0, 6, 1, 5, 3 };
        perm.insert(1, 2);
        for (int i = 0; i < newInput.length; i++) {
            System.out.println("perm.get(" + i + ")");
            System.out.println(perm.get(i));
            assertEquals(newInput[i], perm.get(i));
        }
        int[] finalInput = { 0, 5, 3, 1, 7, 2, 6, 4 };
        perm.insert(0, 0);

        for (int i = 0; i < finalInput.length; i++) {
            System.out.println(perm.get(i));
            assertEquals(finalInput[i], perm.get(i));
        }
    }
}
