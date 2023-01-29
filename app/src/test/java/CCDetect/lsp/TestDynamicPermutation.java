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
        DynamicPermutation perm = new DynamicPermutation(input);
        System.out.println("aTree: " + perm.aTree.getRoot());
        System.out.println("bTree: " + perm.bTree.getRoot());
        System.out.println("arr: " + Printer.print(perm.toArray()));
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        System.out.println();
        int[] newInput = { 0, 2, 1, 3, 4 };
        perm.insert(1, 2);
        System.out.println("aTree: " + perm.aTree.getRoot());
        System.out.println("bTree: " + perm.bTree.getRoot());
        System.out.println("arr: " + Printer.print(perm.toArray()));
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
    }

    @Test
    public void testDynamicPermutation() {
        int[] input = { 3, 0, 5, 1, 4, 2 };
        DynamicPermutation perm = new DynamicPermutation(input);
        System.out.println("aTree: " + perm.aTree.getRoot());
        System.out.println("bTree: " + perm.bTree.getRoot());
        System.out.println("arr: " + Printer.print(perm.toArray()));
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        System.out.println();
        int[] newInput = { 4, 2, 0, 6, 1, 5, 3 };
        perm.insert(1, 2);
        System.out.println("aTree: " + perm.aTree.getRoot());
        System.out.println("bTree: " + perm.bTree.getRoot());
        System.out.println("arr: " + Printer.print(perm.toArray()));
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
        int[] finalInput = { 0, 5, 3, 1, 7, 2, 6, 4 };
        perm.insert(0, 0);

        for (int i = 0; i < finalInput.length; i++) {
            System.out.println(perm.get(i));
            assertEquals(finalInput[i], perm.get(i));
        }
    }

    @Test
    public void testDeleteInDynamicPermutation() {
        int[] input = { 3, 0, 5, 1, 4, 2 };
        DynamicPermutation perm = new DynamicPermutation(input);
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], perm.get(i));
        }
        System.out.println();
        System.out.println("Deleting on index 0");
        perm.delete(0);
        System.out.println("aTree: " + perm.aTree.getRoot());
        System.out.println("bTree: " + perm.bTree.getRoot());
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
        DynamicPermutation perm = new DynamicPermutation(sa);
        for (int i = 0; i < sa.length; i++) {
            assertEquals(sa[i], perm.get(i));
            assertEquals(isa[i], perm.getInverse(i));
        }
    }

    @Test
    public void testDeleteAndInsert() {
        int[] first = { 3, 0, 5, 1, 4, 2 };
        int[] newInput = { 4, 2, 0, 6, 1, 5, 3 };
        DynamicPermutation perm = new DynamicPermutation(first);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], perm.get(i));
        }
        perm.insert(1, 2);
        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
        perm.delete(1);
        for (int i = 0; i < first.length; i++) {
            assertEquals(first[i], perm.get(i));
        }
        perm.insert(1, 2);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], perm.get(i));
        }
    }

    @Test
    public void testEdgecase() {
        int[] first = { 8, 5, 1, 3, 0, 6, 2, 7, 4 };
        int[] newInput = {};
        DynamicPermutation perm = new DynamicPermutation(first);
    }
}
