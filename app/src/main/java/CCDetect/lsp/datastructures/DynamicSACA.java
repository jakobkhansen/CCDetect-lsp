package CCDetect.lsp.datastructures;

import java.util.logging.Logger;

import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.rankselect.WaveletMatrix;

/**
 * DynamicSA
 */
public class DynamicSACA {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    int EXTRA_SIZE_INCREASE = 200;

    DynamicPermutation sa;

    CharacterCount charCounts;
    WaveletMatrix waveletMatrix;

    // Assume initial arrays are of same size
    // Creates a dynamic suffix array datastructure with initialSize potential size
    public DynamicSACA(int[] initialText, int[] initialSA, int[] initialLCP) {
        charCounts = new CharacterCount(initialText);
        sa = new DynamicPermutation(initialSA, initialLCP);

        int[] l = calculateL(initialSA, initialText, initialText.length);
        waveletMatrix = new WaveletMatrix(l, l.length + 100);
    }

    public DynamicSACA(int[] initialText, ExtendedSuffixArray initialESuff) {
        this(initialText, initialESuff.getSuffix(), initialESuff.getLcp());
    }

    public DynamicPermutation getSA() {
        return sa;
    }

    public ExtendedSuffixArray buildESuff() {
        int[] saArr = sa.toArray();
        int[] isaArr = sa.inverseToArray();
        int[] lcpArr = sa.lcpToArray();

        return new ExtendedSuffixArray(saArr, isaArr, lcpArr);
    }

    // Inserts a factor into the suffix array at position [start, end] (inclusive)
    public void insertFactor(EditOperation edit) {
        int[] newText = edit.getChars().stream().mapToInt(i -> i).toArray();
        int position = edit.getPosition();

        int end = newText.length - 1;

        // Stage 2, replace in L
        int posFirstModified = sa.getInverse(position);
        int previousCS = getLF(posFirstModified);

        int storedLetter = waveletMatrix.get(posFirstModified);
        substituteInL(newText[end], posFirstModified);

        int pointOfInsertion = getLF(posFirstModified);
        // Number of smaller characters is one off if the char we have stored is less
        // than the one we inserted
        pointOfInsertion += storedLetter < newText[end] ? 1 : 0;

        // Stage 3, Insert new rows in L
        for (int i = end - 1; i >= 0; i--) {

            insertInL(newText[i], pointOfInsertion);

            // Increment previousCS and/or posFirstModified if we inserted before them
            previousCS += pointOfInsertion <= previousCS ? 1 : 0;
            posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

            sa.insert(pointOfInsertion, position, -1);

            int oldPOS = pointOfInsertion;
            pointOfInsertion = getLF(pointOfInsertion);
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

        sa.insert(pointOfInsertion, position, -1);

        previousCS += pointOfInsertion <= previousCS ? 1 : 0;
        posFirstModified += pointOfInsertion <= posFirstModified ? 1 : 0;

        // Stage 4
        int pos = previousCS;
        int expectedPos = getLF(pointOfInsertion);

        int shift = 0;
        while (pos != expectedPos) {
            int newPos = getLF(pos);
            moveRow(pos, expectedPos);
            pos = newPos;
            expectedPos = getLF(expectedPos);
            shift++;
        }

        updateLCP(expectedPos, shift);

    }

    public void deleteFactor(EditOperation edit) {
        int position = edit.getPosition();
        int length = edit.getChars().size();

        // Stage 2, replace in L (but not actually)
        int posFirstModified = sa.getInverse(position + length);
        int deletedLetter = waveletMatrix.get(posFirstModified);

        int pointOfDeletion = getLF(posFirstModified);

        // Stage 3, Delete rows in L
        for (int i = 0; i < length - 1; i++) {

            int currentLetter = waveletMatrix.get(pointOfDeletion);
            int tmp_rank = getWaveletRank(pointOfDeletion);
            // Rank could be one off since T[i-1] is in L twice at this point
            if (posFirstModified < pointOfDeletion && deletedLetter == currentLetter) {
                tmp_rank--;
            }

            // Delete rows
            deleteInL(pointOfDeletion);
            sa.delete(pointOfDeletion);

            // Update posFirstModified if it has moved because of deletion
            posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

            pointOfDeletion = tmp_rank + getCharsBefore(currentLetter);
            // Rank is one off potentially since T[i-1] is twice in L at this point
            pointOfDeletion -= deletedLetter < currentLetter ? 1 : 0;
        }
        int currentLetter = waveletMatrix.get(pointOfDeletion);
        int tmp_rank = getWaveletRank(pointOfDeletion);
        if (posFirstModified < pointOfDeletion && deletedLetter == currentLetter) {
            tmp_rank--;
        }

        deleteInL(pointOfDeletion);
        sa.delete(pointOfDeletion);

        posFirstModified -= pointOfDeletion <= posFirstModified ? 1 : 0;

        int previousCS = tmp_rank + getCharsBefore(currentLetter);
        previousCS -= deletedLetter < currentLetter ? 1 : 0;

        // Substitute last character
        pointOfDeletion = posFirstModified;
        substituteInL(currentLetter, pointOfDeletion);

        pointOfDeletion = getLF(pointOfDeletion);

        // Stage 4
        int pos = previousCS;
        int expectedPos = pointOfDeletion;

        int shift = 0;
        while (pos != expectedPos) {
            int newPos = getLF(pos);
            moveRow(pos, expectedPos);
            pos = newPos;
            expectedPos = getLF(expectedPos);
            shift++;
        }

        updateLCP(expectedPos, shift);
    }

    // Returns L in an array with custom extra size
    public static int[] calculateL(int[] suff, int[] text, int size) {
        int[] l = new int[size];

        for (int i = 0; i < suff.length; i++) {
            l[i] = text[Math.floorMod(suff[i] - 1, suff.length)];
        }
        return l;
    }

    public int getLF(int index) {

        int charsBefore = getCharsBefore(waveletMatrix.get(index));
        int rank = getWaveletRank(index);
        return charsBefore + rank;
    }

    public int getInverseLF(int index) {
        int i = 0;
        int j = 0;
        while ((j + charCounts.getCharCount(i) <= index)) {
            j += charCounts.getCharCount(i);
            i++;
        }
        int res = waveletMatrix.select(index - j, i);
        return res;
    }

    private int getCharsBefore(int ch) {
        return charCounts.getNumberOfSmallerChars(ch);
    }

    public int getWaveletRank(int position) {
        return waveletMatrix.rank(position);
    }

    private void moveRow(int i, int j) {
        int lValue = waveletMatrix.get(i);
        waveletMatrix.delete(i);
        waveletMatrix.insert(j, lValue);

        int permValue = sa.get(i);
        sa.delete(i);
        sa.insert(j, permValue, -1);

    }

    private void insertInL(int ch, int position) {
        charCounts.addChar(ch);
        waveletMatrix.insert(position, ch);
    }

    private void deleteInL(int position) {
        charCounts.deleteChar(waveletMatrix.get(position));
        waveletMatrix.delete(position);
    }

    private void substituteInL(int ch, int position) {

        charCounts.deleteChar(waveletMatrix.get(position));
        charCounts.addChar(ch);
        waveletMatrix.delete(position);
        waveletMatrix.insert(position, ch);
    }

    public void updateLCP(int startPos, int shift) {
        int pos;
        while ((pos = sa.positionsToUpdate.select(0, true)) != -1) {

            sa.positionsToUpdate.set(pos, false);
            if (pos >= sa.size()) {
                continue;
            }

            updateLCPValue(pos, 0);
        }

        int cs = startPos;
        boolean hasToUpdate = true;
        int numExtraIterations = 10;
        int isFinishing = 0;
        while (numExtraIterations > 0) {

            if (!hasToUpdate) {
                isFinishing = 1;
            }
            numExtraIterations -= isFinishing;

            hasToUpdate = false;
            if (cs != 0) {
                int oldLCP = sa.getLCPValue(cs);
                if (oldLCP >= shift) {
                    hasToUpdate = updateLCPValue(cs, shift);
                }
            }
            if (cs < sa.aTree.size() - 1) {
                hasToUpdate = updateLCPValue(cs + 1, shift) || hasToUpdate;
            }

            cs = getLF(cs);
            shift++;

        }

    }

    public boolean updateLCPValue(int pos, int lowerBound) {
        int oldLCP = sa.getLCPValue(pos);

        if (oldLCP < 0 || oldLCP >= lowerBound) {
            int currentSuffixCS = getInverseLF(pos);
            int prevSuffixCS = getInverseLF(pos - 1);
            int lcpValue = 0;

            int currentSuffixCSValue = waveletMatrix.get(currentSuffixCS);
            int prevSuffixCSValue = waveletMatrix.get(prevSuffixCS);
            while (currentSuffixCSValue == prevSuffixCSValue && currentSuffixCSValue != 1) {
                lcpValue++;
                currentSuffixCS = getInverseLF(currentSuffixCS);
                prevSuffixCS = getInverseLF(prevSuffixCS);
                currentSuffixCSValue = waveletMatrix.get(currentSuffixCS);
                prevSuffixCSValue = waveletMatrix.get(prevSuffixCS);
            }
            if (oldLCP != lcpValue) {
                sa.setLCPValue(pos, lcpValue);
                return true;
            }
        }
        return false;
    }
}
