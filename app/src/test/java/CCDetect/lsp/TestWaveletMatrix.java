package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;

/**
 * TestWaveletMatrix
 */
public class TestWaveletMatrix {

    @Test
    public void testWaveletMatrix() {
        int[] input = { 4, 7, 6, 5, 3, 2, 1, 0, 1, 4, 1, 7 };
        WaveletMatrix matrix = new WaveletMatrix(input, 10);
        for (int i = 0; i < input.length; i++) {
            assertEquals(input[i], matrix.access(i));
        }
    }
}
