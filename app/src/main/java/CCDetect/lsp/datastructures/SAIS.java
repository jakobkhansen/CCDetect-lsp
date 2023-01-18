package CCDetect.lsp.datastructures;

/**
 * SAIS
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Scanner;
import java.util.logging.Logger;

public class SAIS {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public int[] buildSuffixArray(final int[] text, final int alphabetSize) {
        final BitSet suffixTypes = getSuffixTypes(text);
        final int[] bucketSizes = getBucketSizes(text, alphabetSize);
        final int n = text.length;
        final int[] suffixArray = new int[n + 1];
        Arrays.fill(suffixArray, -1);
        positionLmsCharacters(suffixArray, text, suffixTypes, bucketSizes);
        inductionSortL(suffixArray, text, suffixTypes, bucketSizes);
        inductionSortS(suffixArray, text, suffixTypes, bucketSizes);
        final ReductionSummary summary = reduce(suffixArray, text, suffixTypes);
        final int[] summarySuffixArray = buildSummarySuffixArray(summary);
        Arrays.fill(suffixArray, -1);
        positionLmsCharacters(suffixArray, text, suffixTypes, bucketSizes, summarySuffixArray, summary.offsets);
        inductionSortL(suffixArray, text, suffixTypes, bucketSizes);
        inductionSortS(suffixArray, text, suffixTypes, bucketSizes);
        return suffixArray;
    }

    private void positionLmsCharacters(final int[] suffixArray, final int[] text, final BitSet suffixTypes,
            final int[] bucketSizes, final int[] summarySuffixArray, final int[] summaryOffsets) {
        suffixArray[0] = text.length;
        final int[] bucketTails = getBucketTails(bucketSizes);
        for (int i = summarySuffixArray.length - 1; i > 1; i--) {
            final int charIndex = summaryOffsets[summarySuffixArray[i]];
            final int bucketIndex = text[charIndex];
            suffixArray[bucketTails[bucketIndex]] = charIndex;
            bucketTails[bucketIndex]--;
        }
    }

    private int[] buildSummarySuffixArray(final ReductionSummary summary) {
        if (summary.alphabetSize == summary.reducedText.length) {
            int[] suffixArray = new int[summary.reducedText.length + 1];
            suffixArray[0] = summary.reducedText.length;
            for (int i = 1; i < summary.reducedText.length; i++) {
                suffixArray[summary.reducedText[i] + 1] = i;
            }
            return suffixArray;
        }
        return buildSuffixArray(summary.reducedText, summary.alphabetSize);
    }

    private ReductionSummary reduce(final int[] suffixArray, final int[] text, final BitSet suffixTypes) {
        final int[] lmsNames = new int[text.length + 1];
        Arrays.fill(lmsNames, -1);
        int curName = 0;
        int count = 1;
        lmsNames[suffixArray[0]] = curName;
        int curOffset, prevOffset = suffixArray[0];
        for (int i = 1; i < suffixArray.length; i++) {
            if (!isLmsCharacter(suffixArray[i], suffixTypes))
                continue;
            curOffset = suffixArray[i];
            if (!areLmsBlocksEqual(text, prevOffset, curOffset, suffixTypes))
                curName++;
            prevOffset = curOffset;
            lmsNames[curOffset] = curName;
            count++;
        }
        final int[] reducedText = new int[count];
        final int[] offsets = new int[count];
        for (int i = 0, j = 0; i < lmsNames.length; i++) {
            if (lmsNames[i] == -1)
                continue;
            reducedText[j] = lmsNames[i];
            offsets[j] = i;
            j++;
        }
        return new ReductionSummary(reducedText, offsets, curName + 1);
    }

    private boolean areLmsBlocksEqual(final int[] text, final int prevOffset, final int curOffset,
            final BitSet suffixTypes) {
        if (prevOffset == text.length || curOffset == text.length)
            return false;
        if (text[prevOffset] != text[curOffset])
            return false;
        int i = 1;
        while (i < text.length) {
            final boolean prevIsLms = isLmsCharacter(prevOffset + i, suffixTypes);
            final boolean curIsLms = isLmsCharacter(curOffset + i, suffixTypes);
            if (prevIsLms && curIsLms)
                return true;
            if (prevIsLms != curIsLms)
                return false;
            if (text[prevOffset + i] != text[curOffset + i])
                return false;
            i++;
        }
        return false;
    }

    private void inductionSortS(final int[] suffixArray, final int[] text, final BitSet suffixTypes,
            final int[] bucketSizes) {
        final int[] bucketTails = getBucketTails(bucketSizes);
        for (int i = suffixArray.length - 1; i >= 0; i--) {
            int j = suffixArray[i] - 1;
            if (j < 0 || suffixTypes.get(j) != s_type)
                continue;
            suffixArray[bucketTails[text[j]]] = j;
            bucketTails[text[j]]--;
        }
    }

    private void inductionSortL(final int[] suffixArray, final int[] text, final BitSet suffixTypes,
            final int[] bucketSizes) {
        final int[] bucketHeads = getBucketHeads(bucketSizes);
        for (int i = 0; i < suffixArray.length; i++) {
            int j = suffixArray[i] - 1;
            if (j < 0 || suffixTypes.get(j) != l_type)
                continue;
            suffixArray[bucketHeads[text[j]]] = j;
            bucketHeads[text[j]]++;
        }
    }

    private void positionLmsCharacters(final int[] suffixArray, final int[] text, final BitSet suffixTypes,
            final int[] bucketSizes) {
        suffixArray[0] = text.length;
        final int[] bucketTails = getBucketTails(bucketSizes);
        for (int i = text.length - 1; i >= 0; i--) {
            if (!isLmsCharacter(i, suffixTypes))
                continue;
            suffixArray[bucketTails[text[i]]] = i;
            bucketTails[text[i]]--;
        }
    }

    private boolean isLmsCharacter(final int index, final BitSet suffixTypes) {
        if (index == 0)
            return false;
        return suffixTypes.get(index) == s_type && suffixTypes.get(index - 1) == l_type;
    }

    private int[] getBucketHeads(final int[] bucketSizes) {
        final int[] heads = new int[bucketSizes.length];
        int offset = 1;
        for (int i = 0; i < bucketSizes.length; i++) {
            heads[i] = offset;
            offset += bucketSizes[i];
        }
        return heads;
    }

    private int[] getBucketTails(final int[] bucketSizes) {
        final int[] tails = new int[bucketSizes.length];
        int offset = 1;
        for (int i = 0; i < bucketSizes.length; i++) {
            offset += bucketSizes[i];
            tails[i] = offset - 1;
        }
        return tails;
    }

    private int[] getBucketSizes(final int[] text, final int alphabetSize) {
        final int[] sizes = new int[alphabetSize];
        for (final int ch : text) {
            sizes[ch]++;
        }
        return sizes;
    }

    private BitSet getSuffixTypes(final int[] text) {
        final int n = text.length;
        final BitSet types = new BitSet(n + 1);
        types.set(n, s_type);
        if (n == 0)
            return types;
        types.set(n - 1, l_type);
        for (int i = n - 2; i >= 0; i--) {
            if (text[i] < text[i + 1]) {
                types.set(i, s_type);
            } else if (text[i] > text[i + 1]) {
                types.set(i, l_type);
            } else {
                types.set(i, types.get(i + 1));
            }
        }
        return types;
    }

    private static final class ReductionSummary {
        private final int[] reducedText;
        private final int[] offsets;
        private final int alphabetSize;

        private ReductionSummary(final int[] reducedText, final int[] offsets, final int alphabetSize) {
            this.reducedText = reducedText;
            this.offsets = offsets;
            this.alphabetSize = alphabetSize;
        }
    }

    // Inverse

    public int[] buildInverseSuffixArray(int[] suffixArray) {
        int[] inverse = new int[suffixArray.length];
        for (int i = 0; i < suffixArray.length; i++) {
            inverse[suffixArray[i]] = i;
        }

        return inverse;
    }

    public int[] buildLCPArray(int[] T, int[] L, int[] R) {
        int n = T.length;
        int[] lcp = new int[n];

        int l = 0;
        for (int i = 0; i < n - 1; i++) {
            int r = R[i];
            int prevSuffix = L[r - 1];
            while (T[i + l] == T[prevSuffix + l] && T[i + l] != 1) {
                l++;
            }
            lcp[r] = l;
            l = Integer.max(l - 1, 0);

        }

        return lcp;
    }

    // Int array is expected to end with a 0 and have no 0's otherwise
    public ExtendedSuffixArray buildExtendedSuffixArray(int[] text) {
        int max = 0;
        for (int i : text) {
            max = Integer.max(i, max);
        }
        int[] suffOriginal = buildSuffixArray(text, max + 1);

        // Suffix array algorithm for some reason returns 1 element more than input,
        // probably assumes it appends another $ or something
        // TODO refactor so we don't need to do this crap
        int[] suff = new int[suffOriginal.length - 1];
        for (int z = 1; z < suffOriginal.length; z++) {
            suff[z - 1] = suffOriginal[z];
        }

        int[] inverse = buildInverseSuffixArray(suff);

        int[] lcp = buildLCPArray(text, suff, inverse);

        return new ExtendedSuffixArray(suff, inverse, lcp);
    }

    private static final boolean s_type = true;
    private static final boolean l_type = false;

    // Testing

}
