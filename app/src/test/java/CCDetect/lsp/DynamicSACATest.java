package CCDetect.lsp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import CCDetect.lsp.datastructures.DynamicSACA;
import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.utils.Printer;

/**
 * DynamicSATest
 */
public class DynamicSACATest {

    @Test
    public void testUpdateSingleCharacter() {
        SAIS sais = new SAIS();
        DynamicSACA dynSACA = new DynamicSACA();
        String originalText = "ctctgc";
        System.out.println("originalText: " + originalText);
        int[] original = stringToIntArray(originalText);
        ExtendedSuffixArray eSuffBanana = sais.buildExtendedSuffixArray(original);
        int[] updated = stringToIntArray("ctgctgc");
        ExtendedSuffixArray eSuffUpdated = dynSACA.insertSingleChar(eSuffBanana, original, updated, 2);

        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(updated);

        System.out.println("Expected SA: " + Printer.print(expected.getSuffix()));
        System.out.println("Actual SA: " + Printer.print(eSuffUpdated.getSuffix()));
        // assertArrayEquals(expected.getSuffix(), eSuffUpdated.getSuffix());

        System.out.println("Expected ISA: " + Printer.print(expected.getInverseSuffix()));
        System.out.println("Actual ISA: " + Printer.print(eSuffUpdated.getInverseSuffix()));

        assertArrayEquals(expected.getInverseSuffix(),
                eSuffUpdated.getInverseSuffix());
    }

    public int[] stringToIntArray(String input) {
        return IntStream.concat(input.chars().map(c -> {
            return (int) c - ('a' - 1);
        }), IntStream.of(0)).toArray();
    }
}
