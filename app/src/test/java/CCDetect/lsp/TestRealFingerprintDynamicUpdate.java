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
import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.utils.Timer;

public class TestRealFingerprintDynamicUpdate {

    public void testFile(String path) throws Exception {

        File file = new File(path);

        BufferedReader reader = new BufferedReader(new FileReader(file));

        Iterator<String> iter = reader.lines().iterator();
        iter.next();
        int[] oldFingerprint = Arrays.stream(iter.next().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        ExtendedSuffixArray old = buildOldSuffix(iter);

        int[] newFingerprint = Arrays.stream(iter.next().split("\\s+")).mapToInt(Integer::parseInt).toArray();
        int updateIndex = Integer.parseInt(iter.next());
        System.out.println("updateIndex " + updateIndex);

        SAIS sais = new SAIS();
        Timer linearTimer = new Timer();
        linearTimer.start();
        ExtendedSuffixArray expected = sais.buildExtendedSuffixArray(newFingerprint);
        linearTimer.stop();

        DynamicSACA dynSACA = new DynamicSACA();
        Timer incrementalTimer = new Timer();
        incrementalTimer.start();
        ExtendedSuffixArray dynUpdated = dynSACA.insertSingleChar(old, oldFingerprint, newFingerprint,
                updateIndex);
        incrementalTimer.stop();

        linearTimer.log("Linear time");
        incrementalTimer.log("Incremental time");

        assertArrayEquals(expected.getSuffix(), dynUpdated.getSuffix());
        assertArrayEquals(expected.getInverseSuffix(), dynUpdated.getInverseSuffix());

        reader.close();
    }

    @Test
    public void testCCDetectFingerprint() throws Exception {
        testFile("src/test/resources/Fingerprints/ccdetect.txt");
    }

    @Test
    public void testWorldWindFingerprint() throws Exception {
        testFile("src/test/resources/Fingerprints/worldwind.txt");
    }

    public ExtendedSuffixArray buildOldSuffix(Iterator<String> iter) throws Exception {
        String oldSALine = iter.next();
        int[] oldSA = Arrays.stream(oldSALine.split("\\s+")).mapToInt(Integer::parseInt).toArray();

        String oldISALine = iter.next();
        int[] oldISA = Arrays.stream(oldISALine.split("\\s+")).mapToInt(Integer::parseInt).toArray();

        String oldLCPLine = iter.next();
        int[] oldLCP = Arrays.stream(oldLCPLine.split("\\s+")).mapToInt(Integer::parseInt).toArray();

        return new ExtendedSuffixArray(oldSA, oldISA, oldLCP);
    }
}
