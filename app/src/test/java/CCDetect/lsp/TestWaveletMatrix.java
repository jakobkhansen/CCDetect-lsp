package CCDetect.lsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;

/**
 * TestWaveletMatrix
 */
public class TestWaveletMatrix {

    @Test
    public void testAccess() {
        int[] input = { 4, 7, 6, 5, 3, 2, 1, 0, 2, 1, 4, 1, 7 };
        WaveletMatrix matrix = new WaveletMatrix(input, 10);
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
}
