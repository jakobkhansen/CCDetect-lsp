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

    int EXTRA_SIZE_INCREASE = 200;

    int[] l;
    int[] sa;
    int[] isa;
    int[] lcp;
    int arraySize = 0;
    int actualSize = 0;

    // Assume initial arrays are of same size
    // Creates a dynamic suffix array datastructure with initialSize potential size
    public DynamicSACA(int[] initialText, int[] initialSA, int[] initialISA, int[] initialLCP, int initialSize) {
        arraySize = initialSize;
        actualSize = initialText.length;
        sa = new int[arraySize];
        isa = new int[arraySize];
        lcp = new int[arraySize];

        for (int i = 0; i < initialText.length; i++) {
            sa[i] = initialSA[i];
            isa[i] = initialISA[i];
            lcp[i] = initialLCP[i];
        }
        l = calculateL(initialSA, initialText, initialSize);
    }

    public DynamicSACA(int[] initialText, ExtendedSuffixArray initialESuff, int initialSize) {
        this(initialText, initialESuff.getSuffix(), initialESuff.getInverseSuffix(), initialESuff.getLcp(),
                initialSize);
    }

    public void updateSizes(int newSize) {
        if (newSize > arraySize) {
            int newArraySize = newSize + EXTRA_SIZE_INCREASE;
            resizeArrays(newArraySize);
            arraySize = newArraySize;
        }
        actualSize = newSize;
    }

    public void resizeArrays(int newSize) {
        int oldSize = actualSize;
        int[] newSA = new int[newSize];
        int[] newISA = new int[newSize];
        int[] newLCP = new int[newSize];
        int[] newL = new int[newSize];

        for (int i = 0; i < oldSize; i++) {
            newSA[i] = sa[i];
            newISA[i] = isa[i];
            newLCP[i] = lcp[i];
            newL[i] = l[i];
        }
        sa = newSA;
        isa = newISA;
        lcp = newLCP;
        l = newL;
    }

    public ExtendedSuffixArray getExtendedSuffixArray(int[] fingerprint) {
        int[] smallSA = new int[actualSize];
        int[] smallISA = new int[actualSize];
        int[] smallLCP = new int[actualSize];

        for (int i = 0; i < actualSize; i++) {
            smallSA[i] = sa[i];
            smallISA[i] = isa[i];
            smallLCP[i] = lcp[i];
        }
        LOGGER.info("actualSize " + actualSize);
        LOGGER.info("fingerprint size " + fingerprint.length);
        LOGGER.info("sa " + Printer.print(smallSA));
        LOGGER.info("isa " + Printer.print(smallISA));

        // Placeholder LCP array since we aren't dynamically updating yet
        SAIS sais = new SAIS();
        int[] lcp = sais.buildLCPArray(fingerprint, smallSA, smallISA);
        return new ExtendedSuffixArray(smallSA, smallISA, lcp);
    }

    // Inserts a factor into the suffix array at position [start, end] (inclusive)
    public void insertFactor(int[] newText, int position) {
        int oldSize = actualSize;
        int newSize = actualSize + newText.length;
        updateSizes(newSize);
        int end = newText.length - 1;

        // Stage 2, replace in L
        int posFirstModified = isa[position];
        int previousCS = getLFDynamic(posFirstModified, l, oldSize);

        int storedLetter = l[isa[position]];
        l[isa[position]] = newText[end];

        int pointOfInsertion = getLFDynamic(isa[position], l, oldSize);
        // Number of smaller characters is one off if the char we have stored is less
        // than the one we inserted
        pointOfInsertion += storedLetter < newText[end] ? 1 : 0;

        // Stage 3, Insert new rows in L
        for (int i = end - 1; i >= 0; i--) {

            insert(l, pointOfInsertion, newText[i]);

            int l_length = newSize - (i + 1);

            // Increment previousCS and/or posFirstModified if we inserted before them
            previousCS += pointOfInsertion <= previousCS ? 1 : 0;
            posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

            // Increment all values in SA greater than or equal to position
            for (int j = 0; j < l_length; j++) {
                sa[j] += sa[j] >= position ? 1 : 0;
            }

            insert(sa, pointOfInsertion, position);

            // Increment all values in ISA greater than or equal to LF(ISA[position])
            for (int j = 0; j < l_length; j++) {
                isa[j] += isa[j] >= pointOfInsertion ? 1 : 0;
            }

            // Insert new row in ISA
            insert(isa, position, pointOfInsertion);

            int oldPOS = pointOfInsertion;
            pointOfInsertion = getLFDynamic(pointOfInsertion, l, l_length);
            // Again number of smaller characters is one off potentially
            pointOfInsertion += storedLetter < newText[i] ? 1 : 0;

            // Rank is one off if the character is the same and inserted after the stored
            // char
            if (posFirstModified < oldPOS && newText[i] == storedLetter) {
                pointOfInsertion++;
            }

        }
        // Inserting final character that we substituted before

        insert(l, pointOfInsertion, storedLetter);
        previousCS += pointOfInsertion <= previousCS ? 1 : 0;
        posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

        // Update SA
        for (int i = 0; i < newSize; i++) {
            sa[i] += sa[i] >= position ? 1 : 0;
        }

        // Updates ISA
        for (int i = 0; i < newSize; i++) {
            isa[i] += isa[i] >= pointOfInsertion ? 1 : 0;
        }
        insert(sa, pointOfInsertion, position);
        insert(isa, position, pointOfInsertion);

        // Stage 4
        int pos = previousCS;
        int expectedPos = getLFDynamic(pointOfInsertion, l, newSize);

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, newSize);
            moveRow(pos, expectedPos, sa, isa, l);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, newSize);
        }
    }

    public void deleteFactor(int[] oldText, int position, int length) {
        int oldSize = actualSize;
        int newSize = actualSize - length;
        updateSizes(newSize);
        LOGGER.info("old L " + Printer.print(l, oldSize));

        // Stage 2, replace in L
        int posFirstModified = isa[position + length];
        LOGGER.info("posFirstModified: " + posFirstModified);

        int previousCS = getLFDynamic(posFirstModified, l, oldSize);
        int pointOfDeletion = getLFDynamic(isa[position + length], l, oldSize);
        pointOfDeletion -= oldText[position] == l[posFirstModified] ? 1 : 0;

        int deletedLetter = l[posFirstModified];
        l[posFirstModified] = oldText[position - 1];
        LOGGER.info(
                "Replaced " + deletedLetter + " with " + l[posFirstModified] + " at position "
                        + posFirstModified);
        LOGGER.info("L after substitute " + Printer.print(l, oldSize));

        // Rank is one off since we have T[i-1] inserted twice atm
        LOGGER.info("POS initial: " + pointOfDeletion);

        // Stage 3, Delete rows in L
        for (int i = 0; i < length; i++) {

            int l_length = oldSize - i - 1;
            delete(l, pointOfDeletion, l_length);

            LOGGER.info("L after deletion " + Printer.print(l, l_length));

            // Increment all values in SA greater than or equal to position
            for (int j = 0; j < l_length; j++) {
                sa[j] -= sa[j] >= position ? 1 : 0;
            }

            delete(sa, pointOfDeletion, l_length);

            // Increment all values in ISA greater than or equal to LF(ISA[position])
            for (int j = 0; j < l_length; j++) {
                isa[j] -= isa[j] >= pointOfDeletion ? 1 : 0;
            }

            // Insert new row in ISA
            delete(isa, position, l_length);

            // Decrement previousCS and/or posFirstModified if we inserted before them
            previousCS -= pointOfDeletion <= previousCS ? 1 : 0;
            posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

            // int oldPOS = pointOfInsertion;
            pointOfDeletion = getLFDynamic(pointOfDeletion, l, l_length);
            // Again rank is one off potentially
            pointOfDeletion -= oldText[position] == l[posFirstModified] ? 1 : 0;

            // Rank is one off if the character is the same and inserted after the stored
            // char
            // if (posFirstModified < oldPOS && newText[i] == storedLetter) {
            // pointOfInsertion++;
            // }

            LOGGER.info("POS: " + pointOfDeletion);
        }
        LOGGER.info("SA after deletions: " + Printer.print(sa, newSize));
        LOGGER.info("ISA after deletions: " + Printer.print(isa, newSize));
        LOGGER.info("L after moveRow" + Printer.print(l, newSize));

        // Substitute last character TODO replace delete+insert with substitute
        pointOfDeletion = posFirstModified;
        delete(l, pointOfDeletion, newSize);
        insert(l, pointOfDeletion, deletedLetter);

        // Stage 4
        int pos = previousCS;
        int expectedPos = getLFDynamic(pointOfDeletion, l, newSize);

        LOGGER.info("pos " + pos);
        LOGGER.info("expectedPos " + expectedPos);
        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, newSize);
            moveRow(pos, expectedPos, sa, isa, l);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, newSize);
        }
        LOGGER.info("SA after moveRow: " + Printer.print(sa, newSize));
        LOGGER.info("ISA after moveRow: " + Printer.print(isa, newSize));
        LOGGER.info("L after moveRow: " + Printer.print(l, newSize));
    }

    // Returns L in an array with custom extra size
    public static int[] calculateL(int[] suff, int[] text, int size) {
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

    public void setL(int[] sa, int[] text) {
        l = calculateL(sa, text, text.length);
    }

    public static int getLFDynamic(int index, int[] l, int size) {

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

    private void delete(int[] arr, int index, int size) {
        LOGGER.info("Deleting char " + arr[index] + " at position " + index);
        for (int i = index; i < size; i++) {
            arr[i] = arr[i + 1];
        }
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
