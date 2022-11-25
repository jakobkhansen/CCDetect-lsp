package CCDetect.lsp.datastructures;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import CCDetect.lsp.utils.Printer;

/**
 * DynamicSA
 */
public class DynamicSACA {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public ExtendedSuffixArray insertSingleChar(ExtendedSuffixArray suff, int[] oldText, int[] newText, int position) {
        LOGGER.info("oldText: " + Printer.print(oldText));
        LOGGER.info("newText: " + Printer.print(newText));
        LOGGER.info("oldSuffix: " + Printer.print(suff.getSuffix()));
        LOGGER.info("oldISA: " + Printer.print(suff.getInverseSuffix()));
        int[] oldSA = suff.getSuffix();
        int[] oldISA = suff.getInverseSuffix();
        int[] newSA = new int[oldSA.length + 1];
        int[] newISA = new int[oldISA.length + 1];
        int[] l = getL(suff, oldText);

        // Stage 1, copy elements which are not changed
        for (int i = 0; i < oldSA.length; i++) {
            newSA[i] = suff.getSuffix()[i];
            newISA[newSA[i]] = i;
        }

        // Stage 2, update L
        int storedLetter = l[oldISA[position]];
        l[oldISA[position]] = newText[position];

        // Stage 3, insert new row, increasing SA at all values larger than the location
        // its inserted
        int pos = getLFDynamic(newISA[position], oldSA, l, oldText);

        System.out.println("here " + storedLetter);
        System.out.println(Printer.print(l));
        System.out.println(pos);

        // Insert new row in L
        for (int i = l.length - 2; i >= pos; i--) {
            l[i + 1] = l[i];
        }
        l[pos] = storedLetter;

        // Increment all values in SA greater than or equal to position
        for (int i = position; i < oldISA.length; i++) {
            newSA[oldISA[i]]++;
        }

        // Increment all values in ISA greater than or equal to LF(ISA[position])
        for (int i = pos; i < oldSA.length; i++) {
            newISA[oldSA[i]]++;
        }

        // Insert new row in SA
        for (int i = newSA.length - 2; i >= pos; i--) {
            newSA[i + 1] = newSA[i];
        }
        newSA[pos] = position;

        // Insert new row in ISA
        for (int i = newISA.length - 2; i >= newSA[pos]; i--) {
            newISA[i + 1] = newISA[i];
        }
        newISA[newSA[pos]] = pos;

        if (pos >= position) {
            System.out.println("Incremented");
            pos++;
        }
        System.out.println("Suff before stage 4 " + Printer.print(newSA));
        System.out.println("pos " + pos);

        // Stage 4
        int posLF = getLFDynamic(pos - 1, newSA, l, newText);
        System.out.println("posLF " + posLF);
        System.exit(0);
        while (pos != posLF) {
            System.out.println(pos);
            System.out.println(posLF);
            int newPos = getLFDynamic(pos, newSA, l, newText);
            moveRow(pos, posLF, newSA, newISA, l);
            pos = newPos;
            posLF = getLFDynamic(posLF, newSA, l, newText);
        }

        return new ExtendedSuffixArray(newSA, newISA, new SAIS().buildLCPArray(newText, newSA, newISA));
    }

    private int[] getL(ExtendedSuffixArray suff, int[] oldText) {
        int[] l = new int[suff.getSuffix().length + 1];

        for (int i = 0; i < l.length - 1; i++) {
            l[i] = oldText[Math.floorMod(suff.getSuffix()[i] - 1, suff.getSuffix().length)];
        }
        return l;
    }

    private int getLFDynamic(int index, int[] sa, int[] l, int[] text) {

        int charsBefore = 0;
        for (int i = 0; i < text.length; i++) {
            charsBefore += text[i] < l[index] ? 1 : 0;
        }

        int rank = 0;
        for (int i = 0; i < index; i++) {
            rank += l[i] == l[index] ? 1 : 0;
        }
        return charsBefore + rank;
    }

    private void moveRow(int i, int j, int[] newSA, int[] newISA, int[] l) {
        System.out.println("moveRow " + i + " " + j);
        if (i > j) {
            int tmp = j;
            j = i;
            i = tmp;
        }
        System.out.println("SA Setting " + i + " to " + j);
        // Update L
        int tmp = l[i];
        l[i] = l[j];
        l[j] = tmp;

        // Update SA
        int iPos = newSA[i];
        int temp = newSA[j];
        newSA[j] = newSA[i];
        newSA[i] = temp;

        // Update ISA
        for (int index = i; index < j; index++) {
            System.out.println("ISA Decrementing " + newSA[index]);
            newISA[newSA[index]]--;
        }
        System.out.println("setting " + iPos + " to " + j);
        newISA[iPos] = j;
    }
}
