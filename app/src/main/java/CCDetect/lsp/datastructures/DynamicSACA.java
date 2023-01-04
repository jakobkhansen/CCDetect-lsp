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

        // Get the point where the new row will be inserted
        int pointOfInsertion = getLFDynamic(oldISA[position], l, l.length - 1);
        // Correct position since storedLetter could have an effect on LF
        pointOfInsertion += storedLetter <= newText[position] ? 1 : 0;

        // Stage 3, insert new row, increasing SA at all values larger than the location
        // its inserted

        // Insert new row in L
        insert(l, pointOfInsertion, storedLetter);

        // Increment all values in SA greater than or equal to position
        for (int i = position; i < oldISA.length; i++) {
            newSA[oldISA[i]]++;
        }

        // Increment all values in ISA greater than or equal to LF(ISA[position])
        for (int i = pointOfInsertion; i < oldSA.length; i++) {
            newISA[oldSA[i]]++;
        }

        // Insert new row in SA
        insert(newSA, pointOfInsertion, position);

        // Insert new row in ISA
        insert(newISA, position, pointOfInsertion);

        // Stage 4
        int pos = originalPos + (originalPos >= pointOfInsertion ? 1 : 0);
        int expectedPos = getLFDynamic(pointOfInsertion, l, l.length);
        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, l.length);
            moveRow(pos, expectedPos, newSA, newISA, l);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, l.length);
        }

        return new ExtendedSuffixArray(newSA, newISA, new SAIS().buildLCPArray(newText, newSA, newISA));
    }

    // Inserts a factor into the suffix array at position [start, end] (inclusive)
    public ExtendedSuffixArray insertFactor(ExtendedSuffixArray suff, int[] oldText, int[] newText, int start,
            int end) {
        int insertLength = (end - start) + 1;
        int[] oldSA = suff.getSuffix();
        int[] oldISA = suff.getInverseSuffix();

        int[] newSA = new int[oldSA.length + insertLength];
        int[] newISA = new int[oldISA.length + insertLength];

        int[] l = getL(oldSA, oldText, newSA.length);
        System.out.println("Old text " + Printer.print(oldText));
        System.out.println("Old SA " + Printer.print(oldSA));
        System.out.println("Old ISA " + Printer.print(oldISA));
        System.out.println("New text " + Printer.print(newText));
        System.out.println("Old L " + Printer.print(l));
        System.out.println("start=" + start + " end=" + end);

        // Stage 1, copy elements which are not changed
        for (int i = 0; i < oldSA.length; i++) {
            newSA[i] = suff.getSuffix()[i];
            newISA[newSA[i]] = i;
        }

        // Stage 2, replace in L
        int originalPos = getLFDynamic(oldISA[start], l, l.length - insertLength);

        int storedLetter = l[oldISA[start]];
        l[oldISA[start]] = newText[end];

        int pointOfInsertion = getLFDynamic(oldISA[start], l, l.length - insertLength);
        pointOfInsertion += storedLetter <= newText[end] ? 1 : 0;
        System.out.println("pointOfInsertion " + pointOfInsertion);
        System.out.println("L after stage 2 " + Printer.print(l));

        // Stage 3, Insert new rows in L
        for (int i = end - 1; i >= start; i--) {
            insert(l, pointOfInsertion, newText[i]);
            // Increment all values in SA greater than or equal to position
            for (int j = 0; j < newSA.length; j++) {
                newSA[j] += newSA[j] >= start ? 1 : 0;
            }
            insert(newSA, pointOfInsertion, start);

            // Increment all values in ISA greater than or equal to LF(ISA[position])
            for (int j = 0; j < newISA.length; j++) {
                newISA[j] += newISA[j] >= pointOfInsertion ? 1 : 0;
            }

            // Insert new row in ISA
            insert(newISA, start, pointOfInsertion);
        }
        insert(l, pointOfInsertion, storedLetter);
        for (int i = 0; i < newSA.length; i++) {
            newSA[i] += newSA[i] >= start ? 1 : 0;
        }
        for (int j = 0; j < newISA.length; j++) {
            newISA[j] += newISA[j] >= pointOfInsertion ? 1 : 0;
        }
        insert(newSA, pointOfInsertion, start);
        insert(newISA, start, pointOfInsertion);

        System.out.println("L after stage 3 " + Printer.print(l));
        System.out.println("SA after stage 3 " + Printer.print(newSA));
        System.out.println("ISA after stage 3 " + Printer.print(newISA));

        // Stage 4
        int pos = originalPos + (originalPos >= pointOfInsertion ? insertLength : 0);
        int expectedPos = getLFDynamic(pointOfInsertion, l, l.length);
        while (pos != expectedPos) {
            System.out.println("pos " + pos);
            System.out.println("expectedPos " + expectedPos);
            int newPos = getLFDynamic(pos, l, l.length);
            moveRow(pos, expectedPos, newSA, newISA, l);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, l.length);
        }
        System.out.println("L after stage 4 " + Printer.print(l));

        return new ExtendedSuffixArray(newSA, newISA, suff.getLcp());
    }

    // Returns L in an array with custom extra size
    public int[] getL(int[] suff, int[] text, int size) {
        int[] l = new int[size];

        for (int i = 0; i < suff.length; i++) {
            l[i] = text[Math.floorMod(suff[i] - 1, suff.length)];
        }
        // For logging purposes, TODO DELETE THIS
        for (int i = text.length; i < size; i++) {
            l[i] = -1;
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
