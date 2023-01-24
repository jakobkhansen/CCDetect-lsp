package CCDetect.lsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;
import CCDetect.lsp.utils.Printer;

/**
 * TestWaveletMatrix
 */
public class TestWaveletMatrix {

    @Test
    public void testAccess() {
        int[] input = { 4, 7, 6, 5, 3, 2, 1, 0, 1, 4, 1, 7 };
        WaveletMatrix matrix = new WaveletMatrix(input, 10);
        System.out.println(Printer.print(matrix));
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], matrix.access(i));
        }
    }

    @Test
    public void testAccessSmall() {
        int[] input = { 1, 2, 3 };
        WaveletMatrix matrix = new WaveletMatrix(input, 10);
        System.out.println(Printer.print(matrix));
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], matrix.access(i));
        }
    }

    @Test
    public void testRank() {
        int[] input = { 4, 7, 6, 5, 3, 2, 1, 0, 2, 1, 4, 1, 7 };
        int[] ranks = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 1 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        for (int i = 0; i < input.length; i++) {
            assertEquals(ranks[i], matrix.rank(i));
        }
    }

    @Test
    public void testWaveletInsert() {
        int[] input = { 2, 0, 1 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);

        System.out.println(Printer.print(matrix));
        matrix.insert(1, 2);
        System.out.println(Printer.print(matrix));

        assertEquals(2, matrix.access(0));
        assertEquals(2, matrix.access(1));
        assertEquals(0, matrix.access(2));
        assertEquals(1, matrix.access(3));
    }

    @Test
    public void testWaveletInsertMany() {
        int[] input = { 2, 0, 1, 5 };
        int[] newInput = { 4, 2, 0, 1, 5 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        System.out.println("Before");
        System.out.println(Printer.print(matrix));
        matrix.insert(0, 4);
        System.out.println("After: " + Printer.print(matrix));
        System.out.println("Expected: " + Printer.print(new WaveletMatrix(newInput, 20)));

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], matrix.access(i));
        }
    }
}
