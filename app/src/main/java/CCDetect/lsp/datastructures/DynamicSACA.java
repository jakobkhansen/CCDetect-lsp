package CCDetect.lsp.datastructures;

import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;

/**
 * DynamicSA
 */
public class DynamicSACA {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public ExtendedSuffixArray insertSingleChar(ExtendedSuffixArray suff, int[] oldText, int[] newText, int position) {
        // System.out.println("oldText: " + Printer.print(oldText));
        // System.out.println("newText: " + Printer.print(newText));
        // LOGGER.info("oldSuffix: " + Printer.print(suff.getSuffix()));
        // LOGGER.info("oldISA: " + Printer.print(suff.getInverseSuffix()));
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
        System.out.println("L before overwrite " + Printer.print(l));
        int storedLetter = l[oldISA[position]];
        l[oldISA[position]] = newText[position];
        System.out.println();

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

        System.out.println("L before stage 4 " + Printer.print(l));
        System.out.println("SA before stage 4 " + Printer.print(newSA));
        System.out.println("ISA before stage 4 " + Printer.print(newISA));
        // Stage 4

        // I think the problem is that posLF is not calculated correctly, according to
        // Reorder function in dyn suff paper
        // Look at how LF(index(T'[i])) is calculated

        int expectedPos = getLFDynamic(pointOfInsertion, l, l.length);
        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, l.length);
            moveRow(pos, expectedPos, newSA, newISA, l);
            System.out.println("l after moverow " + Printer.print(l));
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, l.length);
        }

        System.out.println("l " + Printer.print(l));
        return new ExtendedSuffixArray(newSA, newISA, suff.getLcp());
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

        int charsBefore = 0;
        for (int i = 0; i < size; i++) {
            charsBefore += l[i] < l[index] ? 1 : 0;
        }
        int rank = 0;
        for (int i = 0; i < index; i++) {
            rank += l[i] == l[index] ? 1 : 0;
        }
        return charsBefore + rank;
    }

    private void moveRow(int i, int j, int[] newSA, int[] newISA, int[] l) {
        System.out.println("moveRow " + i + " " + j);
        // System.out.println("SA Setting " + i + " to " + j);
        // Update L
        move(l, i, j);

        // Update SA
        int iPos = newSA[i];

        move(newSA, i, j);

        // Update ISA
        for (int index = i; index < j; index++) {
            // System.out.println("ISA Decrementing " + newSA[index]);
            newISA[newSA[index]]--;
        }
        // System.out.println("setting " + iPos + " to " + j);
        newISA[iPos] = j;
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
