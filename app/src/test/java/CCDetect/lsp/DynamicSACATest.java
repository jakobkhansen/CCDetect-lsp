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
import CCDetect.lsp.utils.Timer;

/**
 * DynamicSATest
 */
public class DynamicSACATest {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    @Test
    public void testLF() {
        String input = "pneumonoultramicroscopicsilicovolcanoconiosis";
        int[] inputArr = stringToIntArrayWithTerminator(input);
        SAIS sais = new SAIS();
        ExtendedSuffixArray suff = sais.buildExtendedSuffixArray(inputArr);

        int[] l = DynamicSACA.calculateL(suff.getSuffix(), inputArr, suff.getSuffix().length);

        int current = 0;
        boolean found = false;
        for (int i = 0; i < inputArr.length; i++) {
            if (DynamicSACA.getLFDynamic(i, l, l.length) == 0) {
                current = i;
                found = true;
            }
        }
        assertTrue(found);
        System.out.println(current);
        for (int i = inputArr.length - 1; i >= 0; i--) {
            assertEquals(inputArr[i], l[current]);
            current = DynamicSACA.getLFDynamic(current, l, l.length);
        }
    }

    public void testDynamicSuffixInsertSingle(String input, String edit, int position) {
        SAIS sais = new SAIS();
        int[] original = stringToIntArrayWithTerminator(input);
        ExtendedSuffixArray eSuffBanana = sais.buildExtendedSuffixArray(original);
        int[] insert = stringToIntArray(edit);
        DynamicSACA dynSACA = new DynamicSACA(original, eSuffBanana.getSuffix(), eSuffBanana.getInverseSuffix(),
                eSuffBanana.getLcp(), eSuffBanana.size() + 100);

        Timer linearTimer = new Timer();
        linearTimer.start();
        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(insert);
        linearTimer.stop();

        Timer incrementalTimer = new Timer();
        incrementalTimer.start();

        dynSACA.insertFactor(insert, position);
        ExtendedSuffixArray eSuffUpdated = dynSACA.getExtendedSuffixArray();
        incrementalTimer.stop();

        // linearTimer.log("Linear time");
        // incrementalTimer.log("Incremental time");

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

    public void testInsertOnAllIndices(String input, String edit) {
        for (int i = 0; i <= input.length(); i++) {
            testDynamicSuffixInsertFactor(input, edit, i);
        }
    }

    @Test
    public void testInsertSingleCharacter() {
        // testDynamicSuffixInsertSingle("ctctgc", "ctgctgc", 2);
        // testDynamicSuffixInsertSingle("atgcg", "attgcg", 1);
    }

    @Test

    public void testShortSingleInsert() {
        // testDynamicSuffixInsertSingle("cacgacg", "cacagacg", 3);
        // testDynamicSuffixInsertSingle("ab", "aab", 0);
        // testDynamicSuffixInsertSingle("ab", "aab", 1);
        // testDynamicSuffixInsertSingle("ab", "aab", 1);
        // testDynamicSuffixInsertSingle("dc", "dbc", 1);
        // testDynamicSuffixInsertSingle("ab", "axb", 1);
        // testDynamicSuffixInsertSingle("abc", "axbc", 1);
        // testDynamicSuffixInsertSingle("axbc", "axbcd", 4);
        // testDynamicSuffixInsertSingle("axbcd", "axbcda", 5);
        //
        // testDynamicSuffixInsertSingle("mississippi", "missiissippi", 4);
        // testDynamicSuffixInsertSingle("mississippi", "missiissippi", 5);
        // testDynamicSuffixInsertSingle("pneumonoultramicroscopicsilicovolcanoconiosis",
        // "pneumonoulxtramicroscopicsilicovolcanoconiosis", 10);
    }

    @Test
    public void testInsertAllIndices() {
        // testInsertOnAllIndices("helloworld", "a");
        // testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis", "a");
        // testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis", "x");
        // testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis", "h");
    }

    public void testDynamicSuffixInsertFactor(String input, String edit, int position) {
        // Build arrays
        SAIS sais = new SAIS();
        int[] originalArray = stringToIntArrayWithTerminator(input);
        ExtendedSuffixArray eSuffBanana = sais.buildExtendedSuffixArray(originalArray);
        int[] editArray = stringToIntArray(getStringWithEdit(input, edit, position));
        int[] resultArray = insertIntoIntArray(originalArray, editArray, position);

        System.out.println("HERE");
        System.out.println(Printer.print(editArray));
        System.out.println(Printer.print(resultArray));

        // Build expected result suffix array
        Timer linearTimer = new Timer();
        linearTimer.start();
        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(resultArray);
        linearTimer.stop();

        // Dynamically update original
        Timer incrementalTimer = new Timer();
        incrementalTimer.start();
        DynamicSACA dynSACA = new DynamicSACA(originalArray, eSuffBanana, eSuffBanana.size() + 100);
        dynSACA.insertFactor(editArray, position);
        ExtendedSuffixArray eSuffUpdated = dynSACA.getExtendedSuffixArray();
        incrementalTimer.stop();

        linearTimer.log("Linear time");
        incrementalTimer.log("Incremental time");

        int[] l = DynamicSACA.calculateL(expected.getSuffix(), resultArray, expected.getSuffix().length);
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
    public void testInsertSmallFactor() {
        testDynamicSuffixInsertFactor("b", "a", 0);
        // testDynamicSuffixInsertFactor("ctctgc", "g", 2);
        // testDynamicSuffixInsertFactor("b", "bac", 1);
        // testDynamicSuffixInsertFactor("b", "bacd", 1);
        // testDynamicSuffixInsertFactor("b", "ab", 0);
        // testDynamicSuffixInsertFactor("b", "abb", 0);
        // testDynamicSuffixInsertFactor("ac", "adac", 1);
        // testDynamicSuffixInsertFactor("ab", "abab", 2);
        // testDynamicSuffixInsertFactor("b", "bbb", 0);
        // testDynamicSuffixInsertFactor("b", "asldkjsalkdjb", 01);
        // testDynamicSuffixInsertFactor("bcd", "bbcadcd", 1);
        // testDynamicSuffixInsertFactor("bcd", "bcdxxx", 3);
        // testDynamicSuffixInsertFactor("bcd", "bxadpxcd", 1);
        // testDynamicSuffixInsertFactor("bcd", "bccdd", 2);
        // testInsertOnAllIndices("bcd", "hx");
        // testInsertOnAllIndices("bcd", "aj");

    }

    @Test
    public void testInsertFactorOnAllIndices() {
        // testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis",
        // "habc");
        // testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis",
        // "xxxxxasldkjsadoiqw");
    }

    public int[] stringToIntArrayWithTerminator(String input) {
        return IntStream.concat(input.chars().map(c -> {
            return (int) c - ('a' - 1);
        }), IntStream.of(0)).toArray();
    }

    public int[] stringToIntArray(String input) {
        return input.chars().map(c -> {
            return (int) c - ('a' - 1);
        }).toArray();
    }

    public int[] insertIntoIntArray(int[] input, int[] insert, int position) {
        int[] result = new int[input.length + insert.length];
        int index = 0;
        for (int i = 0; i < position; i++) {
            result[index] = input[i];
            index++;
        }
        for (int i = 0; i < insert.length; i++) {
            result[index] = insert[i];
            index++;
        }
        for (int i = position; i < input.length; i++) {
            result[index] = input[i];
            index++;
        }

        return result;
    }

    public String getStringWithEdit(String input, String edit, int position) {
        return input.substring(0, position) + edit + input.substring(position);
    }

}
