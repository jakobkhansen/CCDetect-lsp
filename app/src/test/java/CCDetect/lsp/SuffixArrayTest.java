package CCDetect.lsp;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import CCDetect.lsp.datastructures.SAIS;

/**
 * SuffixArrayTest
 */
public class SuffixArrayTest {

    @Test
    public void testBanana() {
        int[] input = stringToIntArrayWithTerminator("banana");
        int max = Arrays.stream(input).max().getAsInt();

        SAIS sais = new SAIS();
        int[] suff = sais.buildSuffixArray(input, max + 1);
        assertArrayEquals(new int[] { 6, 5, 3, 1, 0, 4, 2 }, suff);
    }

    public void testMississippi() {
        int[] input = stringToIntArrayWithTerminator("mississippi");
        int max = Arrays.stream(input).max().getAsInt();
        SAIS sais = new SAIS();
        int[] suff = sais.buildSuffixArray(input, max + 1);

        assertArrayEquals(new int[] { 10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2 }, suff);

    }

    public void testVeryLongWord() {
        int[] input = stringToIntArrayWithTerminator("pneumonoultramicroscopicsilicovolcanoconiosis");
        int max = Arrays.stream(input).max().getAsInt();
        SAIS sais = new SAIS();
        int[] suff = sais.buildSuffixArray(input, max + 1);

        assertArrayEquals(new int[] { 5, 12, 34, 33, 37, 19, 28, 15, 23, 2, 27, 14, 22, 25, 40, 43, 32, 26, 9, 13, 4, 1,
                39, 35, 6, 36, 31, 38, 5, 20, 17, 41, 7, 29, 21, 0, 11, 16, 44, 18, 24, 42, 10, 8, 3, 30
        }, suff);

    }

    public int[] stringToIntArrayWithTerminator(String input) {
        return input.chars().map(c -> {
            return (int) c - 'a';
        }).toArray();
    }
}
