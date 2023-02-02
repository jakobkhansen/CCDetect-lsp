package CCDetect.lsp;

import static org.junit.Assert.assertArrayEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import org.junit.Test;

import CCDetect.lsp.datastructures.DynamicSACA;
import CCDetect.lsp.datastructures.ExtendedSuffixArray;
import CCDetect.lsp.datastructures.SAIS;
import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.editdistance.EditOperationType;
import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.utils.Timer;

public class TestRealFingerprintDynamicUpdate {

    public void testFile(String path) throws Exception {

        File file = new File(path);

        BufferedReader reader = new BufferedReader(new FileReader(file));

        Iterator<String> iter = reader.lines().iterator();
        iter.next();
        int[] oldFingerprint = Arrays.stream(iter.next().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        ExtendedSuffixArray old = buildOldSuffix(iter, oldFingerprint);

        int[] edit = Arrays.stream(iter.next().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int position = Integer.parseInt(iter.next());

        SAIS sais = new SAIS();
        Timer linearTimer = new Timer();
        linearTimer.start();
        int[] newArray = insertIntoIntArray(oldFingerprint, edit, position);
        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(newArray);
        linearTimer.stop();

        DynamicSACA dynSACA = new DynamicSACA(oldFingerprint, old);
        Timer incrementalTimer = new Timer();
        incrementalTimer.start();
        dynSACA.insertFactor(new EditOperation(EditOperationType.INSERT, position, edit));
        incrementalTimer.stop();
        ExtendedSuffixArray dynUpdated = dynSACA.buildESuff();

        linearTimer.log("Linear time");
        incrementalTimer.log("Incremental time");

        System.out.println("insert " + position);
        System.out.println("old size: " + oldFingerprint.length);
        System.out.println("new size: " + newArray.length);

        assertArrayEquals(expected.getSuffix(), dynUpdated.getSuffix());
        assertArrayEquals(expected.getInverseSuffix(),
                dynUpdated.getInverseSuffix());
        assertArrayEquals(expected.getLcp(),
                dynSACA.getDynLCP().toArray());

        reader.close();
    }

    // TODO reimplement tests
    @Test
    public void testCCDetectFingerprint() throws Exception {
        testFile("src/test/resources/Fingerprints/ccdetect.txt");
    }

    @Test
    public void testWorldWindFingerprint() throws Exception {
        testFile("src/test/resources/Fingerprints/worldwind.txt");
    }

    @Test
    public void testWorldWindFingerprintFactor() throws Exception {
        testFile("src/test/resources/Fingerprints/worldwind_factor.txt");
    }

    public ExtendedSuffixArray buildOldSuffix(Iterator<String> iter, int[] fingerprint) throws Exception {
        String oldSALine = iter.next();
        int[] oldSA = Arrays.stream(oldSALine.split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int[] oldISA = new SAIS().buildInverseSuffixArray(oldSA);
        int[] oldLCP = new SAIS().buildLCPArray(fingerprint, oldSA, oldISA);

        return new ExtendedSuffixArray(oldSA, oldISA, oldLCP);
    }

    public static int[] insertIntoIntArray(int[] input, int[] insert, int position) {
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
}
