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
        LOGGER.info("oldText: " + Printer.print(oldText));
        LOGGER.info("newText: " + Printer.print(newText));
        LOGGER.info("oldSuffix: " + Printer.print(suff.getSuffix()));
        LOGGER.info("oldISA: " + Printer.print(suff.getInverseSuffix()));
        int[] newSA = new int[suff.getSuffix().length + 1];
        int[] newISA = new int[suff.getSuffix().length + 1];

        // Stage 1, copy elements which are not changed
        for (int i = 0; i < position; i++) {
            newSA[i] = suff.getSuffix()[i] + ((suff.getSuffix()[i] < position) ? 0 : 1);
            newISA[newSA[i]] = i;
        }

        // Stage 3, insert new row
        for (int i = position; i < suff.getSuffix().length; i++) {
            newSA[i] = suff.getSuffix()[i] + 1;
        }

        return new ExtendedSuffixArray(newSA, newISA, suff.getLcp());
    }

}
