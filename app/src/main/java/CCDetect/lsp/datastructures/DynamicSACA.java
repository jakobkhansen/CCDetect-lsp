package CCDetect.lsp.datastructures;

import java.util.logging.Logger;

import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;
import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.utils.Timer;

/**
 * DynamicSA
 */
public class DynamicSACA {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    int EXTRA_SIZE_INCREASE = 200;

    DynamicPermutation permutation;
    CharacterCount charCounts;
    WaveletMatrix waveletMatrix;
    int arraySize = 0;
    int actualSize = 0;

    // Assume initial arrays are of same size
    // Creates a dynamic suffix array datastructure with initialSize potential size
    public DynamicSACA(int[] initialText, int[] initialSA, int[] initialISA, int[] initialLCP, int initialSize) {
        arraySize = initialSize;
        actualSize = initialText.length;
        charCounts = new CharacterCount(initialText);
        permutation = new DynamicPermutation(initialSA);

        int[] l = calculateL(initialSA, initialText, initialText.length);
        waveletMatrix = new WaveletMatrix(l, initialSize);
    }

    public DynamicSACA(int[] initialText, ExtendedSuffixArray initialESuff, int initialSize) {
        this(initialText, initialESuff.getSuffix(), initialESuff.getInverseSuffix(), initialESuff.getLcp(),
                initialSize);
    }

    public DynamicPermutation getPermutation() {
        return permutation;
    }

    public DynamicExtendedSuffixArray getESuffFromPermutation(int[] fingerprint) {
        int[] lcp = new SAIS().buildLCPArray(fingerprint, permutation);

        return new DynamicExtendedSuffixArray(permutation, lcp);
    }

    public int[] getLCP(int[] fingerprint) {
        SAIS sais = new SAIS();
        return sais.buildLCPArray(fingerprint, permutation);
    }

    // Inserts a factor into the suffix array at position [start, end] (inclusive)
    public void insertFactor(EditOperation edit) {
        int[] newText = edit.getChars().stream().mapToInt(i -> i).toArray();
        int position = edit.getPosition();

        int newSize = actualSize + newText.length;
        int end = newText.length - 1;

        // Stage 2, replace in L
        int posFirstModified = permutation.getInverse(position);
        int previousCS = getLFDynamic(posFirstModified);

        int storedLetter = waveletMatrix.access(posFirstModified);
        substituteInL(newText[end], posFirstModified);

        int pointOfInsertion = getLFDynamic(posFirstModified);
        // Number of smaller characters is one off if the char we have stored is less
        // than the one we inserted
        pointOfInsertion += storedLetter < newText[end] ? 1 : 0;

        // Stage 3, Insert new rows in L
        for (int i = end - 1; i >= 0; i--) {

            insertInL(newText[i], pointOfInsertion);

            // Increment previousCS and/or posFirstModified if we inserted before them
            previousCS += pointOfInsertion <= previousCS ? 1 : 0;
            posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

            // Insert new rows
            permutation.insert(pointOfInsertion, position);

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

        permutation.insert(pointOfInsertion, position);

        // Stage 4
        int pos = previousCS;
        int expectedPos = getLFDynamic(pointOfInsertion);

        while (pos != expectedPos) {
            int newPos = getLFDynamic(pos);
            moveRow(pos, expectedPos);
            pos = newPos;
            expectedPos = getLFDynamic(expectedPos);
        }
    }

    public void deleteFactor(EditOperation edit) {
        int position = edit.getPosition();
        int length = edit.getChars().size();

        int oldSize = actualSize;
        int newSize = actualSize - length;

        // Stage 2, replace in L (but not actually)
        int posFirstModified = permutation.getInverse(position + length);
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

            // Delete rows
            permutation.delete(pointOfDeletion);

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

        permutation.delete(pointOfDeletion);

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
            moveRow(pos, expectedPos);
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

    private void moveRow(int i, int j) {
        int lValue = waveletMatrix.access(i);
        waveletMatrix.delete(i);
        waveletMatrix.insert(j, lValue);

        int permValue = permutation.get(i);
        permutation.delete(i);
        permutation.insert(j, permValue);

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

}
