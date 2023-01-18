package CCDetect.lsp.datastructures;

import CCDetect.lsp.utils.Printer;

public class SmallerCharacterCounts {
    int[] smallerChars;
    int realSize;

    public SmallerCharacterCounts(int[] chars) {
        realSize = chars.length;
        int max = 0;
        for (int ch : chars) {
            max = ch > max ? ch : max;
        }
        int[] charCounts = new int[max + 1];
        smallerChars = new int[max + 100];

        for (int ch : chars) {
            charCounts[ch]++;
        }

        for (int i = 0; i < charCounts.length; i++) {
            for (int j = i + 1; j < smallerChars.length; j++) {
                smallerChars[j] += charCounts[i];
            }
        }
    }

    public int getNumberOfSmallerChars(int ch) {
        return smallerChars[ch];
    }

    public void deleteChar(int ch) {
        for (int i = ch + 1; i < smallerChars.length; i++) {
            smallerChars[i]--;
        }
    }

    public void addChar(int ch) {
        for (int i = ch + 1; i < smallerChars.length; i++) {
            smallerChars[i]++;
        }
    }
}
