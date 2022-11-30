package CCDetect.lsp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.junit.Test;

import CCDetect.lsp.datastructures.DynamicSACA;
import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.utils.Printer;

/**
 * DynamicSATest
 */
public class DynamicSACATest {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    @Test
    public void testLF() {
        String input = "pneumonoultramicroscopicsilicovolcanoconiosis";
        int[] inputArr = stringToIntArray(input);
        SAIS sais = new SAIS();
        ExtendedSuffixArray suff = sais.buildExtendedSuffixArray(inputArr);

        DynamicSACA saca = new DynamicSACA();
        int[] l = saca.getL(suff.getSuffix(), inputArr, suff.getSuffix().length);

        int current = 0;
        boolean found = false;
        for (int i = 0; i < inputArr.length; i++) {
            if (saca.getLFDynamic(i, l, l.length) == 0) {
                current = i;
                found = true;
            }
        }
        assertTrue(found);
        System.out.println(current);
        for (int i = inputArr.length - 1; i >= 0; i--) {
            assertEquals(inputArr[i], l[current]);
            current = saca.getLFDynamic(current, l, l.length);
        }
    }

    public void testDynamicSuffix(String input, String edit, int position) {
        SAIS sais = new SAIS();
        DynamicSACA dynSACA = new DynamicSACA();
        int[] original = stringToIntArray(input);
        ExtendedSuffixArray eSuffBanana = sais.buildExtendedSuffixArray(original);
        int[] updated = stringToIntArray(edit);

        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(updated);

        ExtendedSuffixArray eSuffUpdated = dynSACA.insertSingleChar(eSuffBanana,
                original, updated, position);

        int[] l = dynSACA.getL(eSuffUpdated.getSuffix(), updated, eSuffUpdated.getSuffix().length);
        System.out.println("Expected L " + Printer.print(l));

        System.out.println("Expected SA: " + Printer.print(expected.getSuffix()));
        System.out.println("Actual SA: " + Printer.print(eSuffUpdated.getSuffix()));
        assertArrayEquals(expected.getSuffix(), eSuffUpdated.getSuffix());

        System.out.println("Expected ISA: " +
                Printer.print(expected.getInverseSuffix()));
        System.out.println("Actual ISA: " +
                Printer.print(eSuffUpdated.getInverseSuffix()));

        assertArrayEquals(expected.getInverseSuffix(),
                eSuffUpdated.getInverseSuffix());

    }

    @Test
    public void testInsertSingleCharacter() {
        testDynamicSuffix("ctctgc", "ctgctgc", 2);
    }

    @Test
    public void testShortSingleInsert() {
        // testDynamicSuffix("ab", "aab", 0);
        // testDynamicSuffix("ab", "aab", 1);
        // testDynamicSuffix("ab", "abb", 1);
        // testDynamicSuffix("ab", "abb", 2);

        testDynamicSuffix("mississippi", "missiissippi", 4);
        testDynamicSuffix("mississippi", "missiissippi", 5);
    }

    public int[] stringToIntArray(String input) {
        return IntStream.concat(input.chars().map(c -> {
            return (int) c - ('a' - 1);
        }), IntStream.of(0)).toArray();
    }

}
