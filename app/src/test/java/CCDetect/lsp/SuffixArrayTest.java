package CCDetect.lsp;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.utils.Printer;

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

    @Test
    public void testMississippi() {
        int[] input = stringToIntArrayWithTerminator("mississippi");
        int max = Arrays.stream(input).max().getAsInt();
        SAIS sais = new SAIS();
        int[] suff = sais.buildSuffixArray(input, max + 1);

        assertArrayEquals(new int[] { 11, 10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2 }, suff);

    }

    @Test
    public void testVeryLongWord() {
        int[] input = stringToIntArrayWithTerminator("pneumonoultramicroscopicsilicovolcanoconiosis");
        int max = Arrays.stream(input).max().getAsInt();
        SAIS sais = new SAIS();
        int[] suff = sais.buildSuffixArray(input, max + 1);

        assertArrayEquals(
                new int[] { 45, 12, 34, 33, 37, 19, 28, 15, 23, 2, 27, 14, 22, 25, 40, 43, 32, 26, 9, 13, 4, 1,
                        39, 35, 6, 36, 31, 38, 5, 20, 17, 41, 7, 29, 21, 0, 11, 16, 44, 18, 24, 42, 10, 8, 3, 30
                }, suff);

    }

    @Test
    public void testDefenseThing() {

        int[] input = new int[] { 2, 3, 4, 5, 3, 6, 7, 8, 9, 6, 10, 11, 1, 2, 3, 14, 5, 3, 6, 7, 8, 9, 6, 15, 11, 1,
                0 };

        SAIS sais = new SAIS();
        ExtendedSuffixArray esuff = sais.buildExtendedSuffixArray(input);
        // System.out.println(Printer.print(esuff.getSuffix()));
        // System.out.println(Printer.print(esuff.getInverseSuffix()));
        // System.out.println(Printer.print(esuff.getLcp()));
    }

    public int[] stringToIntArrayWithTerminator(String input) {
        return input.chars().map(c -> {
            return (int) c - 'a';
        }).toArray();
    }
}
