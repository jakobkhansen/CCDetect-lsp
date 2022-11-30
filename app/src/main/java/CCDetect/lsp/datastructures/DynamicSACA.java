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
        System.out.println("oldText: " + Printer.print(oldText));
        System.out.println("newText: " + Printer.print(newText));
        // LOGGER.info("oldSuffix: " + Printer.print(suff.getSuffix()));
        // LOGGER.info("oldISA: " + Printer.print(suff.getInverseSuffix()));
        int[] oldSA = suff.getSuffix();
        int[] oldISA = suff.getInverseSuffix();
        int[] newSA = new int[oldSA.length + 1];
        int[] newISA = new int[oldISA.length + 1];
        int[] l = getL(oldSA, oldText, newSA.length);
        System.out.println("l: " + Printer.print(l));

        // Stage 1, copy elements which are not changed
        for (int i = 0; i < oldSA.length; i++) {
            newSA[i] = suff.getSuffix()[i];
            newISA[newSA[i]] = i;
        }
        for (int i = 0; i < oldText.length; i++) {
            System.out.println(i + " " + getLFDynamic(i, l, l.length - 1));

        }

        int originalPos = getLFDynamic(oldISA[position], l, l.length - 1);
        System.out.println("originalPos " + originalPos);

        // Stage 2, update L
        int storedLetter = l[oldISA[position]];
        l[oldISA[position]] = newText[position];

        // Stage 3, insert new row, increasing SA at all values larger than the location
        // its inserted

        // Insert new row in L
        insert(l, originalPos, storedLetter);

        // Increment all values in SA greater than or equal to position
        for (int i = position; i < oldISA.length; i++) {
            newSA[oldISA[i]]++;
        }
        int pos = originalPos + (originalPos >= position ? 1 : 0);
        // int pos = getLFDynamic(oldISA[position], l, l.length);
        // int pos = originalPos;

        // Increment all values in ISA greater than or equal to LF(ISA[position])
        for (int i = originalPos; i < oldSA.length; i++) {
            newISA[oldSA[i]]++;
        }

        // Insert new row in SA
        insert(newSA, originalPos, position);

        // Insert new row in ISA
        insert(newISA, newSA[originalPos], originalPos);

        System.out.println("L before stage 4 " + Printer.print(l));
        System.out.println("SA before stage 4 " + Printer.print(newSA));
        System.out.println("ISA before stage 4 " + Printer.print(newISA));
        System.out.println("pos " + pos);
        // Stage 4

        // I think the problem is that posLF is not calculated correctly, according to
        // Reorder function in dyn suff paper
        // Look at how LF(index(T'[i])) is calculated
        int posLF = getLFDynamic((pos - 1), l, l.length);
        while (pos != posLF) {
            int newPos = getLFDynamic(pos, l, l.length);
            moveRow(pos, posLF, newSA, newISA, l);
            pos = newPos;
            posLF = getLFDynamic(posLF, l, l.length);
            System.out.println("pos " + pos);
            System.out.println("posLF " + posLF);
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
        int temp = newSA[j];
        newSA[j] = newSA[i];
        newSA[i] = temp;

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
                arr[i] = arr[i + 1];
            }
        } else {
            for (int k = i; k > j; k--) {
                arr[i] = arr[i - 1];
            }

        }
        // Mo
        arr[j] = item;
    }
}
