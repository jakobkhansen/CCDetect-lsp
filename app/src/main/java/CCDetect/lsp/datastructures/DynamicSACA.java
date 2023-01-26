package CCDetect.lsp.datastructures;

import java.util.logging.Logger;

import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;
import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.utils.Timer;

/**
 * DynamicSA
 */
public class DynamicSACA {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    int EXTRA_SIZE_INCREASE = 200;

    int[] sa;
    int[] isa;
    int[] lcp;
    SmallerCharacterCounts charCounts;
    WaveletMatrix waveletMatrix;
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
        charCounts = new SmallerCharacterCounts(initialText);

        for (int i = 0; i < initialText.length; i++) {
            sa[i] = initialSA[i];
            isa[i] = initialISA[i];
            lcp[i] = initialLCP[i];
        }
        int[] l = calculateL(initialSA, initialText, initialText.length);
        waveletMatrix = new WaveletMatrix(l, initialSize);
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
        LOGGER.info("Resizing dynamic arrays");
        int oldSize = actualSize;
        int[] newSA = new int[newSize];
        int[] newISA = new int[newSize];

        for (int i = 0; i < oldSize; i++) {
            newSA[i] = sa[i];
            newISA[i] = isa[i];
        }
        sa = newSA;
        isa = newISA;
    }

    public ExtendedSuffixArray getSmallExtendedSuffixArray(int[] fingerprint) {
        int[] smallSA = new int[actualSize];
        int[] smallISA = new int[actualSize];

        for (int i = 0; i < actualSize; i++) {
            smallSA[i] = sa[i];
            smallISA[i] = isa[i];
        }

        // Placeholder LCP array since we aren't dynamically updating yet
        SAIS sais = new SAIS();
        Timer lcptimer = new Timer();
        lcptimer.start();
        int[] lcp = sais.buildLCPArray(fingerprint, smallSA, smallISA);
        lcptimer.stop();
        lcptimer.log("Time to build LCP array from scratch");
        return new ExtendedSuffixArray(smallSA, smallISA, lcp);
    }

    public ExtendedSuffixArray getExtendedSuffixArray(int[] fingerprint) {
        SAIS sais = new SAIS();
        Timer lcptimer = new Timer();
        lcptimer.start();
        int[] newLCP = sais.buildLCPArray(fingerprint, sa, isa);
        lcptimer.stop();
        lcptimer.log("Time to build LCP array from scratch");
        return new ExtendedSuffixArray(sa, isa, newLCP, actualSize);
    }

    // Inserts a factor into the suffix array at position [start, end] (inclusive)
    public void insertFactor(EditOperation edit) {
        int[] newText = edit.getChars().stream().mapToInt(i -> i).toArray();
        int position = edit.getPosition();

        int newSize = actualSize + newText.length;
        updateSizes(newSize);
        int end = newText.length - 1;

        // Stage 2, replace in L
        int posFirstModified = isa[position];
        int previousCS = getLFDynamic(posFirstModified);

        int storedLetter = waveletMatrix.access(isa[position]);
        substituteInL(newText[end], isa[position]);

        int pointOfInsertion = getLFDynamic(isa[position]);
        // Number of smaller characters is one off if the char we have stored is less
        // than the one we inserted
        pointOfInsertion += storedLetter < newText[end] ? 1 : 0;

        // Stage 3, Insert new rows in L
        for (int i = end - 1; i >= 0; i--) {

            insertInL(newText[i], pointOfInsertion);
            insertInLCP(newText[i], pointOfInsertion, edit);

            int l_length = newSize - (i + 1);

            // Increment previousCS and/or posFirstModified if we inserted before them
            previousCS += pointOfInsertion <= previousCS ? 1 : 0;
            posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

            int[] positionsToIncrementInISA = new int[l_length - pointOfInsertion - 1];
            for (int j = pointOfInsertion; j < l_length - 1; j++) {
                positionsToIncrementInISA[j - pointOfInsertion] = sa[j];
            }

            // Increment all values in SA greater than or equal to position
            // incrementGreaterThan(sa, position, l_length);
            for (int j = position; j < l_length - 1; j++) {
                sa[isa[j]]++;
            }

            insert(sa, pointOfInsertion, position);

            // Increment all values in ISA greater than or equal to LF(ISA[position])
            // incrementGreaterThan(isa, pointOfInsertion, l_length);
            for (int posToIncrement : positionsToIncrementInISA) {
                isa[posToIncrement]++;
            }

            // Insert new row in ISA
            insert(isa, position, pointOfInsertion);

            int oldPOS = pointOfInsertion;
            pointOfInsertion = getLFDynamic(pointOfInsertion);
            // Again number of smaller characters is one off potentially
            pointOfInsertion += storedLetter < newText[i] ? 1 : 0;

            // Rank is one off if the character is the same and inserted after the stored
            // char
            if (posFirstModified < oldPOS && newText[i] == storedLetter) {
                pointOfInsertion++;
            }

        }
        // Inserting final character that we substituted before

        insertInL(storedLetter, pointOfInsertion);
        previousCS += pointOfInsertion <= previousCS ? 1 : 0;
        posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

        int[] positionsToIncrementInISA = new int[newSize - pointOfInsertion - 1];
        for (int j = pointOfInsertion; j < newSize - 1; j++) {
            positionsToIncrementInISA[j - pointOfInsertion] = sa[j];
        }

        // Increment all values in SA greater than or equal to position
        for (int j = position; j < newSize - 1; j++) {
            sa[isa[j]]++;
        }

        insert(sa, pointOfInsertion, position);

        // Increment all values in ISA greater than or equal to LF(ISA[position])
        for (int posToIncrement : positionsToIncrementInISA) {
            isa[posToIncrement]++;
        }

        // Insert new row in ISA
        insert(isa, position, pointOfInsertion);

        // Stage 4
        int pos = previousCS;
        int expectedPos = getLFDynamic(pointOfInsertion);

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos);
            moveRow(pos, expectedPos, sa, isa);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos);
        }
    }

    public void deleteFactor(EditOperation edit) {
        int position = edit.getPosition();
        int length = edit.getChars().size();

        int oldSize = actualSize;
        int newSize = actualSize - length;
        updateSizes(newSize);

        // Stage 2, replace in L (but not actually)
        int posFirstModified = isa[position + length];
        int deletedLetter = waveletMatrix.access(posFirstModified);

        int pointOfDeletion = getLFDynamic(posFirstModified);

        // Stage 3, Delete rows in L
        for (int i = 0; i < length - 1; i++) {
            int l_length = oldSize - i - 1;

            int currentLetter = waveletMatrix.access(pointOfDeletion);
            int tmp_rank = getWaveletRank(pointOfDeletion);
            // Rank could be one off since T[i-1] is in L twice at this point
            if (posFirstModified < pointOfDeletion && deletedLetter == currentLetter) {
                tmp_rank--;
            }

            deleteInL(pointOfDeletion);

            // Update posFirstModified if it has moved because of deletion
            posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

            // Decrement all values in SA greater than or equal to the value we deleted
            decrementGreaterThan(sa, sa[pointOfDeletion], l_length + 1);

            // Decrement all values in ISA greater than or equal to the value we deleted
            decrementGreaterThan(isa, pointOfDeletion, l_length + 1);

            // Delete rows
            delete(sa, pointOfDeletion, l_length);
            delete(isa, position, l_length);

            pointOfDeletion = tmp_rank + getCharsBefore(currentLetter);
            // Rank is one off potentially since T[i-1] is twice in L at this point
            pointOfDeletion -= deletedLetter < currentLetter ? 1 : 0;
        }
        int currentLetter = waveletMatrix.access(pointOfDeletion);
        int tmp_rank = getWaveletRank(pointOfDeletion);
        if (posFirstModified < pointOfDeletion && deletedLetter == currentLetter) {
            tmp_rank--;
        }

        deleteInL(pointOfDeletion);
        posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

        // Decrement all values in SA greater than or equal to position
        decrementGreaterThan(sa, sa[pointOfDeletion], newSize + 1);

        // Decrement all values in ISA greater than or equal to LF(ISA[position])
        decrementGreaterThan(isa, pointOfDeletion, newSize + 1);

        delete(sa, pointOfDeletion, newSize);
        delete(isa, position, newSize);

        int previousCS = tmp_rank + getCharsBefore(currentLetter);
        previousCS -= deletedLetter < currentLetter ? 1 : 0;

        // Substitute last character
        pointOfDeletion = posFirstModified;
        substituteInL(currentLetter, pointOfDeletion);

        pointOfDeletion = getLFDynamic(pointOfDeletion);

        // Stage 4
        int pos = previousCS;
        int expectedPos = pointOfDeletion;

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos);
            moveRow(pos, expectedPos, sa, isa);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos);
        }
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

    public int getLFDynamic(int index) {

        int charsBefore = getCharsBefore(waveletMatrix.access(index));
        int rank = getWaveletRank(index);
        return charsBefore + rank;
    }

    private int getCharsBefore(int ch) {
        return charCounts.getNumberOfSmallerChars(ch);
    }

    public int getWaveletRank(int position) {
        return waveletMatrix.rank(position);
    }

    private void moveRow(int i, int j, int[] newSA, int[] newISA) {
        int lValue = waveletMatrix.access(i);
        waveletMatrix.delete(i);
        waveletMatrix.insert(j, lValue);

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

    private void decrementGreaterThan(int[] arr, int element, int size) {
        for (int i = 0; i < size; i++) {
            arr[i] -= arr[i] >= element ? 1 : 0;
        }
    }

    private void insertInL(int ch, int position) {
        charCounts.addChar(ch);
        waveletMatrix.insert(position, ch);
    }

    private void deleteInL(int position) {
        charCounts.deleteChar(waveletMatrix.access(position));
        waveletMatrix.delete(position);
    }

    private void substituteInL(int ch, int position) {
        charCounts.deleteChar(waveletMatrix.access(position));
        charCounts.addChar(ch);
        waveletMatrix.delete(position);
        waveletMatrix.insert(position, ch);
    }

    private void insertInLCP(int ch, int position, EditOperation edit) {
        // Need the relevant document fingerprint in order to determine the new lcp
        // value
        if (edit.getDocument() == null) {
            return;
        }

        int[] documentFingerprint = edit.getDocument().getFullFingerprint();
        int fingerprintStart = edit.getDocument().getFingerprintStart();
        int newLCPValue;
        if (position == fingerprintStart) {
            newLCPValue = 0;
        } else {
            for (int i = 0; i < documentFingerprint.length; i++) {

            }
        }

    }
}
