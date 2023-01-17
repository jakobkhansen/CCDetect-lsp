package CCDetect.lsp.datastructures;

import CCDetect.lsp.utils.Printer;

public class CharacterCount {
    int[] characterCounts;
    int realSize;

    public CharacterCount(int[] chars) {
        realSize = chars.length;
        int max = 0;
        for (int ch : chars) {
            max = ch > max ? ch : max;
        }
        characterCounts = new int[max + 100];

        for (int ch : chars) {
            characterCounts[ch]++;
        }
    }

    public int getNumberOfSmallerChars(int ch) {
        int sum = 0;
        for (int i = ch - 1; i >= 0; i--) {
            sum += characterCounts[i];
        }
        return sum;
    }

    public void deleteChar(int ch) {
        characterCounts[ch]--;
    }

    public void addChar(int ch) {
        characterCounts[ch]++;
    }
}
