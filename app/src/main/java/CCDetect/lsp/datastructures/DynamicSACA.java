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
        int[] newSA = new int[suff.getSuffix().length + 1];
        int[] newISA = new int[suff.getSuffix().length + 1];
        Map<Integer, Integer> lf = getLF(suff, oldText);

        // Stage 1, copy elements which are not changed
        for (int i = 0; i < position; i++) {
            newSA[i] = suff.getSuffix()[i];
            newISA[newSA[i]] = i;
        }

        for (int i = position + 1; i < newSA.length; i++) {
            newSA[i] = suff.getSuffix()[i - 1];
            newISA[newSA[i - 1]] = i;
        }

        // Stage 3, insert new row
        // for (int i = position; i < suff.getInverseSuffix().length; i++) {
        // LOGGER.info("here: " + suff.getInverseSuffix()[i]);
        // newSA[newISA[i]]++;
        //
        // }

        return new ExtendedSuffixArray(newSA, newISA, suff.getLcp());
    }

    // TODO this was harder than expected, maybe re-evaulate
    private Map<Integer, Integer> getLF(ExtendedSuffixArray suff, int[] oldText) {
        // Result
        Map<Integer, Integer> lf = new HashMap<>();

        // Used to count number of characters which occur before the first instance of a
        // char
        Map<Integer, Integer> charsBefore = new LinkedHashMap<>();

        // Used to build rank array
        Map<Integer, Integer> charCount = new HashMap<>();

        // Used to determine how many occurences of the same char occur before the
        // character on this index in one in L
        int[] charRank = new int[oldText.length];

        int[] sa = suff.getSuffix();

        // Build charsBefore
        int count = 0;
        for (int i = 0; i < sa.length; i++) {
            charsBefore.put(oldText[sa[i]],
                    Math.min(charsBefore.getOrDefault(oldText[sa[i]], Integer.MAX_VALUE), count));
            count++;
        }

        // Build charRank
        for (int i = 0; i < sa.length; i++) {
            int lCharIndex = Math.floorMod(sa[i] - 1, sa.length);
            charRank[lCharIndex] = charCount.getOrDefault(oldText[lCharIndex], 0);
            charCount.put(oldText[lCharIndex], charCount.getOrDefault(oldText[lCharIndex], 0) + 1);
        }

        // Build LF based on charsBefore and charRank
        for (int i = 0; i < sa.length; i++) {
            int lCharIndex = Math.floorMod(sa[i] - 1, sa.length);

            lf.put(i, charsBefore.get(oldText[lCharIndex]) + charRank[lCharIndex]);

        }

        return lf;
    }
}
