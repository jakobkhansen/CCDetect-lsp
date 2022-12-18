package CCDetect.lsp.datastructures;

import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.utils.Timer;

/**
 * DynamicSA
 */
public class DynamicSACA {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public ExtendedSuffixArray insertSingleChar(ExtendedSuffixArray suff, int[] oldText, int[] newText, int position) {
        int[] oldSA = suff.getSuffix();
        int[] oldISA = suff.getInverseSuffix();
        int[] newSA = new int[oldSA.length + 1];
        int[] newISA = new int[oldISA.length + 1];
        int[] l = getL(oldSA, oldText, newSA.length);

        // Stage 1, copy elements which are not changed
        for (int i = 0; i < oldSA.length; i++) {
            newSA[i] = suff.getSuffix()[i];
            newISA[newSA[i]] = i;
        }

        int originalPos = getLFDynamic(oldISA[position], l, l.length - 1);

        // Stage 2, update L
        int storedLetter = l[oldISA[position]];
        l[oldISA[position]] = newText[position];

        int pointOfInsertion = getLFDynamic(oldISA[position], l, l.length - 1);
        pointOfInsertion += storedLetter <= newText[position] ? 1 : 0;

        // Stage 3, insert new row, increasing SA at all values larger than the location
        // its inserted

        // Insert new row in L
        insert(l, pointOfInsertion, storedLetter);

        // Increment all values in SA greater than or equal to position
        for (int i = position; i < oldISA.length; i++) {
            newSA[oldISA[i]]++;
        }
        int pos = originalPos + (originalPos >= pointOfInsertion ? 1 : 0);
        // int pos = 3;
        // int pos = getLFDynamic(oldISA[position], l, l.length);
        // int pos = originalPos;

        // Increment all values in ISA greater than or equal to LF(ISA[position])
        for (int i = pointOfInsertion; i < oldSA.length; i++) {
            newISA[oldSA[i]]++;
        }

        // Insert new row in SA
        insert(newSA, pointOfInsertion, position);

        // Insert new row in ISA
        insert(newISA, position, pointOfInsertion);

        // Stage 4
        int expectedPos = getLFDynamic(pointOfInsertion, l, l.length);
        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, l.length);
            moveRow(pos, expectedPos, newSA, newISA, l);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, l.length);
        }

        return new ExtendedSuffixArray(newSA, newISA, new SAIS().buildLCPArray(newText, newSA, newISA));
    }

    // Returns L in an array with custom extra size
    public int[] getL(int[] suff, int[] text, int size) {
        int[] l = new int[size];

        for (int i = 0; i < suff.length; i++) {
            l[i] = text[Math.floorMod(suff[i] - 1, suff.length)];
        }
        return l;
    }

    public int getLFDynamic(int index, int[] l, int size) {

        Timer timer = new Timer();
        timer.start();
        int charsBefore = 0;
        for (int i = 0; i < size; i++) {
            charsBefore += l[i] < l[index] ? 1 : 0;
        }
        int rank = 0;
        for (int i = 0; i < index; i++) {
            rank += l[i] == l[index] ? 1 : 0;
        }
        timer.stop();
        timer.log("LF");
        return charsBefore + rank;
    }

    private void moveRow(int i, int j, int[] newSA, int[] newISA, int[] l) {
        move(l, i, j);

        // Update ISA
        if (i < j) {
            for (int index = i + 1; index <= j; index++) {
                newISA[newSA[index]]--;
            }
        } else {
            for (int index = j; index < i; index++) {
                newISA[newSA[index]]++;
            }
        }
        newISA[newSA[i]] = j;

        // Update SA
        move(newSA, i, j);
    }

    private void insert(int[] arr, int index, int element) {
        for (int i = arr.length - 1; i > index; i--) {
            arr[i] = arr[i - 1];
        }
        arr[index] = element;
    }

    // Move arr[i] to index j
    private void move(int[] arr, int i, int j) {
        int item = arr[i];
        if (i < j) {
            for (int k = i; k < j; k++) {
                arr[k] = arr[k + 1];
            }
        } else {
            for (int k = i; k > j; k--) {
                arr[k] = arr[k - 1];
            }
        }
        // Mo
        arr[j] = item;
    }
}
