package CCDetect.lsp.datastructures;

import CCDetect.lsp.utils.Printer;

public class CharacterCount {
    int[] charCounts;
    int realSize;

    public CharacterCount(int[] chars) {
        realSize = chars.length;
        int max = 0;
        for (int ch : chars) {
            max = ch > max ? ch : max;
        }
        charCounts = new int[max + 200];

        for (int ch : chars) {
            charCounts[ch]++;
        }
    }

    public int getNumberOfSmallerChars(int ch) {
        int sum = 0;
        for (int i = ch - 1; i >= 0; i--) {
            sum += charCounts[i];
        }
        return sum;
    }

    public void deleteChar(int ch) {
        charCounts[ch]--;
    }

    public void addChar(int ch) {
        charCounts[ch]++;
    }

    public int getCharCount(int ch) {
        return charCounts[ch];
    }
}
