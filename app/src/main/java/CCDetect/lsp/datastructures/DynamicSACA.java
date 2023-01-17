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
            incrementGreaterThan(sa, position, l_length);

            insert(sa, pointOfInsertion, position);

            // Increment all values in ISA greater than or equal to LF(ISA[position])
            incrementGreaterThan(isa, pointOfInsertion, l_length);

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
        incrementGreaterThan(sa, position, newSize);

        // Updates ISA
        incrementGreaterThan(isa, pointOfInsertion, newSize);

        insert(sa, pointOfInsertion, position);
        insert(isa, position, pointOfInsertion);

        // Stage 4
        int pos = previousCS;
        int expectedPos = getLFDynamic(pointOfInsertion, l, newSize);

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, newSize);
            moveRow(pos, expectedPos);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, newSize);
        }
    }

    public void deleteFactor(int position, int length) {
        int oldSize = actualSize;
        int newSize = actualSize - length;
        updateSizes(newSize);

        // Stage 2, replace in L (but not actually)
        int posFirstModified = isa[position + length];
        int deletedLetter = l[posFirstModified];

        int pointOfDeletion = getLFDynamic(posFirstModified, l, oldSize);

        // Stage 3, Delete rows in L
        for (int i = 0; i < length - 1; i++) {
            int l_length = oldSize - i - 1;

            int tmp_rank = getRank(l, pointOfDeletion, l_length);
            // Rank could be one off since T[i-1] is in L twice at this point
            if (posFirstModified < pointOfDeletion && deletedLetter == l[pointOfDeletion]) {
                tmp_rank--;
            }
            int currentLetter = l[pointOfDeletion];

            delete(l, pointOfDeletion, l_length);

            // Update posFirstModified if it has moved because of deletion
            posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

            // Decrement all values in SA greater than or equal to the value we deleted
            decrementGreaterThan(sa, sa[pointOfDeletion], l_length + 1);

            // Decrement all values in ISA greater than or equal to the value we deleted
            decrementGreaterThan(isa, pointOfDeletion, l_length + 1);

            // Delete rows
            delete(sa, pointOfDeletion, l_length);
            delete(isa, position, l_length);

            pointOfDeletion = tmp_rank + getCharsBefore(l, currentLetter, l_length);
            // Rank is one off potentially since T[i-1] is twice in L at this point
            pointOfDeletion -= deletedLetter < currentLetter ? 1 : 0;
        }
        int currentLetter = l[pointOfDeletion];
        int tmp_rank = getRank(l, pointOfDeletion, newSize);
        if (posFirstModified < pointOfDeletion && deletedLetter == currentLetter) {
            tmp_rank--;
        }

        delete(l, pointOfDeletion, newSize);
        posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

        // Decrement all values in SA greater than or equal to position
        decrementGreaterThan(sa, sa[pointOfDeletion], newSize + 1);

        // Decrement all values in ISA greater than or equal to LF(ISA[position])
        decrementGreaterThan(isa, pointOfDeletion, newSize + 1);

        delete(sa, pointOfDeletion, newSize);
        delete(isa, position, newSize);

        int previousCS = tmp_rank + getCharsBefore(l, currentLetter, newSize);
        previousCS -= deletedLetter < currentLetter ? 1 : 0;

        // Substitute last character
        pointOfDeletion = posFirstModified;
        l[pointOfDeletion] = currentLetter;

        pointOfDeletion = getLFDynamic(pointOfDeletion, l, newSize);

        // Stage 4
        int pos = previousCS;
        int expectedPos = pointOfDeletion;

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, newSize);
            moveRow(pos, expectedPos);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, newSize);
        }
    }

    // Substitutes T[position..substitute.length] with substitute
    public void substituteFactor(int[] substitute, int position) {
        int end = substitute.length - 1;

        // Stage 2, replace in L
        int posFirstModified = isa[position + substitute.length];
        int previousCS = getLFDynamic(posFirstModified, l, actualSize);

        // int storedLetter = l[isa[position]];
        System.out.println("L: " + Printer.print(l, actualSize));

        int previousPoint = posFirstModified;
        int pointOfSubstitution = getLFDynamic(posFirstModified, l, actualSize);

        System.out.println("posFirstModified " + posFirstModified);
        System.out.println("previousPoint " + previousPoint);
        System.out.println("POS initial: " + pointOfSubstitution);

        // Stage 3, Insert new rows in L
        for (int i = end; i > 0; i--) {

            System.out.println("POS " + pointOfSubstitution);
            System.out
                    .println("Substituted " + l[pointOfSubstitution] + " with " + substitute[i]
                            + " at "
                            + pointOfSubstitution);
            l[pointOfSubstitution] = substitute[i];
            previousPoint = getLFDynamic(previousPoint, l, actualSize);
            System.out.println("L: " + Printer.print(l, actualSize));
            System.out.println("Moving " + pointOfSubstitution + " to " + previousPoint);
            moveRow(pointOfSubstitution, previousPoint);
            if (pointOfSubstitution <= previousCS && previousCS <= previousPoint) {
                previousCS--;
            }
            if (pointOfSubstitution >= previousCS && previousCS >= previousPoint) {
                previousCS++;
            }
            System.out.println("L: " + Printer.print(l, actualSize));

            pointOfSubstitution = getLFDynamic(previousPoint, l, actualSize);
        }

        // Inserting final character that we substituted before
        // Stage 4
        int pos = previousCS;
        int expectedPos = getLFDynamic(pointOfSubstitution, l, actualSize);
        System.out.println("pos: " + pos);
        System.out.println("expectedPos: " + expectedPos);

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos, l, actualSize);
            moveRow(pos, expectedPos);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos, l, actualSize);
        }
        System.out.println("Final L: " + Printer.print(l, actualSize));
        System.out.println("Final SA: " + Printer.print(l, actualSize));
        System.out.println("Final ISA: " + Printer.print(l, actualSize));

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

    private void moveRow(int i, int j) {
        move(l, i, j);

        // Update ISA
        if (i < j) {
            for (int index = i + 1; index <= j; index++) {
                isa[sa[index]]--;
            }
        } else {
            for (int index = j; index < i; index++) {
                isa[sa[index]]++;
            }
        }
        isa[sa[i]] = j;

        // Update SA
        move(sa, i, j);
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

    private void incrementGreaterThan(int[] arr, int element, int size) {
        for (int i = 0; i < size; i++) {
            arr[i] += arr[i] >= element ? 1 : 0;
        }
    }

    private void decrementGreaterThan(int[] arr, int element, int size) {
        for (int i = 0; i < size; i++) {
            arr[i] -= arr[i] >= element ? 1 : 0;
        }
    }
}
