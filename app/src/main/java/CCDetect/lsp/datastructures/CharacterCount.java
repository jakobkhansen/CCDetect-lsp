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
        if (ch >= charCounts.length) {
            increaseAlphabetSize(ch);
        }
        charCounts[ch]++;
    }

    public void increaseAlphabetSize(int minSize) {
        int[] newArray = new int[minSize * 2];
        for (int i = 0; i < charCounts.length; i++) {
            newArray[i] = charCounts[i];
        }
        charCounts = newArray;
    }

    public int getCharCount(int ch) {
        return charCounts[ch];
    }
}
