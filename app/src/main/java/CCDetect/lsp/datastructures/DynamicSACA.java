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
        System.out.println("old L " + Printer.print(l, oldSize));
        System.out.println("old SA " + Printer.print(sa, oldSize));
        System.out.println("old ISA " + Printer.print(isa, oldSize));

        // Stage 2, replace in L
        int posFirstModified = isa[position + length];
        System.out.println("posFirstModified: " + posFirstModified);
        int deletedLetter = l[posFirstModified];
        System.out.println("letter substituted= " + deletedLetter);

        int pointOfDeletion = getLFDynamic(posFirstModified, l, oldSize);

        System.out.println("POS initial: " + pointOfDeletion);

        // Rank is one off since we have T[i-1] inserted twice atm

        // Stage 3, Delete rows in L
        for (int i = 0; i < length - 1; i++) {

            int l_length = oldSize - i - 1;
            int tmp_rank = getRank(l, pointOfDeletion, l_length);
            int currentLetter = l[pointOfDeletion];
            System.out.println("tmp_rank " + tmp_rank);
            delete(l, pointOfDeletion, l_length);

            // Update posFirstModified if they have moved because of deletion
            posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

            System.out.println("L after deletion " + Printer.print(l, l_length));

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

            // int oldPOS = pointOfInsertion;
            pointOfDeletion = tmp_rank + getCharsBefore(l, currentLetter, l_length);
            // Rank is one off potentially
            pointOfDeletion -= deletedLetter < currentLetter ? 1 : 0;

            // Rank is one off if the character is the same and inserted after the stored
            // char
            // if (posFirstModified < oldPOS && newText[i] == storedLetter) {
            // pointOfInsertion++;
            // }

            System.out.println("POS: " + pointOfDeletion);
        }
        int currentLetter = l[pointOfDeletion];
        int tmp_rank = getRank(l, pointOfDeletion, newSize);
        System.out.println("tmp_rank " + tmp_rank);
        System.out.println("Deleting " + l[pointOfDeletion] + " at position " + pointOfDeletion);
        delete(l, pointOfDeletion, newSize);
        posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;
        // Increment all values in SA greater than or equal to position
        for (int j = 0; j < newSize; j++) {
            sa[j] -= sa[j] >= position ? 1 : 0;
        }

        delete(sa, pointOfDeletion, newSize);

        // Increment all values in ISA greater than or equal to LF(ISA[position])
        for (int j = 0; j < newSize; j++) {
            isa[j] -= isa[j] >= pointOfDeletion ? 1 : 0;
        }

        // Insert new row in ISA
        delete(isa, position, newSize);

        int previousCS = tmp_rank + getCharsBefore(l, currentLetter, newSize);
        previousCS -= deletedLetter < currentLetter ? 1 : 0;
        System.out.println("previousCS " + previousCS);

        // Substitute last character TODO replace delete+insert with substitute
        pointOfDeletion = posFirstModified;
        System.out.println("Substitution by " + currentLetter + " at position " + pointOfDeletion);
        delete(l, pointOfDeletion, newSize);
        insert(l, pointOfDeletion, currentLetter);
        System.out.println("SA after deletions: " + Printer.print(sa, newSize));
        System.out.println("ISA after deletions: " + Printer.print(isa, newSize));
        System.out.println("L after deletions" + Printer.print(l, newSize));
        pointOfDeletion = getLFDynamic(pointOfDeletion, l, newSize);

        // Stage 4
        int pos = previousCS;
        int expectedPos = pointOfDeletion;

        System.out.println("pos " + pos);
        System.out.println("expectedPos " + expectedPos);
        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, newSize);
            moveRow(pos, expectedPos, sa, isa, l);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, newSize);
        }
        System.out.println("SA after moveRow: " + Printer.print(sa, newSize));
        System.out.println("ISA after moveRow: " + Printer.print(isa, newSize));
        System.out.println("L after moveRow: " + Printer.print(l, newSize));
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
        int charsBefore = getCharsBefore(l, l[index], size);
        int rank = getRank(l, index, size);
        timer.stop();
        return charsBefore + rank;
    }

    private static int getCharsBefore(int[] l, int ch, int size) {
        int charsBefore = 0;
        for (int i = 0; i < size; i++) {
            charsBefore += l[i] < ch ? 1 : 0;
        }
        return charsBefore;
    }

    public static int getRank(int[] l, int index, int size) {
        int rank = 0;
        for (int i = 0; i < index; i++) {
            rank += l[i] == l[index] ? 1 : 0;
        }
        return rank;
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
