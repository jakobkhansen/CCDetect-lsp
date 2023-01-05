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

    public void testDynamicSuffixInsertSingle(String input, String edit, int position) {
        SAIS sais = new SAIS();
        DynamicSACA dynSACA = new DynamicSACA();
        int[] original = stringToIntArray(input);
        ExtendedSuffixArray eSuffBanana = sais.buildExtendedSuffixArray(original);
        int[] updated = stringToIntArray(edit);

        Timer linearTimer = new Timer();
        linearTimer.start();
        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(updated);
        linearTimer.stop();

        Timer incrementalTimer = new Timer();
        incrementalTimer.start();
        ExtendedSuffixArray eSuffUpdated = dynSACA.insertSingleChar(eSuffBanana,
                original, updated, position);
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

    public void testInsertOnAllIndices(String input, String insert) {

        for (int i = 0; i < input.length(); i++) {
            String end = input.substring(i);
            String start = input.substring(0, i);
            String edit = start + insert + end;
            System.out.println("EDIT: " + edit);
            System.out.println("i " + i + " j " + (i + insert.length() - 1));
            testDynamicSuffixInsertFactor(input, edit, i, i + insert.length() - 1);
        }

        // Test end
        String edit = input + insert;
        int start = input.length();
        int end = start + insert.length() - 1;
        testDynamicSuffixInsertFactor(input, edit, start, end);
    }

    @Test
    public void testInsertSingleCharacter() {
        testDynamicSuffixInsertSingle("ctctgc", "ctgctgc", 2);
        testDynamicSuffixInsertSingle("atgcg", "attgcg", 1);
    }

    @Test

    public void testShortSingleInsert() {
        testDynamicSuffixInsertSingle("cacgacg", "cacagacg", 3);
        testDynamicSuffixInsertSingle("ab", "aab", 0);
        testDynamicSuffixInsertSingle("ab", "aab", 1);
        testDynamicSuffixInsertSingle("ab", "aab", 1);
        testDynamicSuffixInsertSingle("dc", "dbc", 1);
        testDynamicSuffixInsertSingle("ab", "axb", 1);
        testDynamicSuffixInsertSingle("abc", "axbc", 1);
        testDynamicSuffixInsertSingle("axbc", "axbcd", 4);
        testDynamicSuffixInsertSingle("axbcd", "axbcda", 5);

        testDynamicSuffixInsertSingle("mississippi", "missiissippi", 4);
        testDynamicSuffixInsertSingle("mississippi", "missiissippi", 5);
        testDynamicSuffixInsertSingle("pneumonoultramicroscopicsilicovolcanoconiosis",
                "pneumonoulxtramicroscopicsilicovolcanoconiosis", 10);
    }

    @Test
    public void testInsertAllIndices() {
        testInsertOnAllIndices("helloworld", "a");
        testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis", "a");
        testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis", "x");
        testInsertOnAllIndices("pneumonoultramicroscopicsilicovolcanoconiosis", "h");
    }

    public void testDynamicSuffixInsertFactor(String input, String edit, int start, int end) {
        SAIS sais = new SAIS();
        DynamicSACA dynSACA = new DynamicSACA();
        int[] original = stringToIntArray(input);
        ExtendedSuffixArray eSuffBanana = sais.buildExtendedSuffixArray(original);
        int[] updated = stringToIntArray(edit);

        Timer linearTimer = new Timer();
        linearTimer.start();
        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(updated);
        linearTimer.stop();

        Timer incrementalTimer = new Timer();
        incrementalTimer.start();
        ExtendedSuffixArray eSuffUpdated = dynSACA.insertFactor(eSuffBanana,
                original, updated, start, end);
        incrementalTimer.stop();

        // linearTimer.log("Linear time");
        // incrementalTimer.log("Incremental time");

        int[] l = dynSACA.getL(expected.getSuffix(), updated, expected.getSuffix().length);
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
        testDynamicSuffixInsertFactor("b", "ab", 0, 0);
        testDynamicSuffixInsertFactor("ctctgc", "ctgctgc", 2, 2);

        testDynamicSuffixInsertFactor("b", "bac", 1, 2);
        testDynamicSuffixInsertFactor("b", "bacd", 1, 3);
        testDynamicSuffixInsertFactor("b", "ab", 0, 0);
        testDynamicSuffixInsertFactor("b", "abb", 0, 1);
        testDynamicSuffixInsertFactor("ac", "adac", 1, 2);
        testDynamicSuffixInsertFactor("ab", "abab", 2, 3);
        testDynamicSuffixInsertFactor("b", "bbb", 0, 1);
        testDynamicSuffixInsertFactor("b", "asldkjsalkdjb", 0, 11);
        testDynamicSuffixInsertFactor("bcd", "bbcadcd", 1, 4);

        testDynamicSuffixInsertFactor("bcd", "bcdxxx", 3, 5);
        testDynamicSuffixInsertFactor("bcd", "bxadpxcd", 1, 5);

        // testDynamicSuffixInsertFactor("bcd", "bcajd", 2, 3);
        // testInsertOnAllIndices("bcd", "hx");
        // testInsertOnAllIndices("bcd", "aj");

    }

    public int[] stringToIntArray(String input) {
        return IntStream.concat(input.chars().map(c -> {
            return (int) c - ('a' - 1);
        }), IntStream.of(0)).toArray();
    }

}
