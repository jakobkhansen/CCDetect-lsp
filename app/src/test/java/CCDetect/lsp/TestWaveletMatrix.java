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
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], matrix.get(i));
        }
    }

    @Test
    public void testAccessSmall() {
        int[] input = { 1, 2, 3 };
        WaveletMatrix matrix = new WaveletMatrix(input, 10);
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], matrix.get(i));
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

        assertEquals(2, matrix.get(0));
        assertEquals(2, matrix.get(1));
        assertEquals(0, matrix.get(2));
        assertEquals(1, matrix.get(3));
    }

    @Test
    public void testWaveletInsertMany() {
        int[] input = { 2, 0, 1, 5 };
        int[] newInput = { 4, 2, 0, 1, 5 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        matrix.insert(0, 4);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], matrix.get(i));
        }
    }

    @Test
    public void testWaveletInsertNewBit() {
        int[] input = { 2, 0, 1 };
        int[] newInput = { 2, 0, 1, 5 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        matrix.insert(3, 5);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], matrix.get(i));
        }
    }

    @Test
    public void testWaveletDelete() {
        int[] input = { 2, 0, 1 };
        int[] newInput = { 2, 1 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        matrix.delete(1);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], matrix.get(i));
        }
    }

    @Test
    public void testWaveletDeleteEdges() {
        int[] input = { 2, 0, 1, 4 };
        int[] middle = { 2, 0, 1 };
        int[] end = { 0, 1 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        matrix.delete(3);

        for (int i = 0; i < middle.length; i++) {
            assertEquals(middle[i], matrix.get(i));
        }
        matrix.delete(0);
        for (int i = 0; i < end.length; i++) {
            assertEquals(end[i], matrix.get(i));
        }
    }

    @Test
    public void testWaveletInsertEdgecase() {
        int[] input = { 3, 4, 0 };
        int[] newInput = { 3, 2, 4, 0 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        matrix.insert(1, 2);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], matrix.get(i));
        }
    }

    @Test
    public void testWaveletInsertEdge() {
        int[] input = { 3, 0 };
        int[] newInput = { 3, 0, 1 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        matrix.insert(2, 1);

        for (int i = 0; i < newInput.length; i++) {
            assertEquals(newInput[i], matrix.get(i));
        }
    }

    @Test
    public void testWaveletSelect() {
        int[] input = { 3, 2, 1, 1, 2, 3, 2, 4, 9 };
        WaveletMatrix matrix = new WaveletMatrix(input, 20);
        assertEquals(1, matrix.select(0, 2));
        assertEquals(4, matrix.select(1, 2));
        assertEquals(0, matrix.select(0, 3));
        assertEquals(2, matrix.select(0, 1));
    }
}
