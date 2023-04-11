package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.editdistance.EditOperationType;
import CCDetect.lsp.datastructures.editdistance.HirschbergsAlgorithm;
import CCDetect.lsp.utils.Printer;

public class TestHirschbergsAlgorithm {
    @Test
    public void testHirschbergsInsert() {
        int[] s1 = { 1, 2, 3, 4, 5 };
        int[] s2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        // String s1 = "ABAB";
        // String s2 = "ABBAB";
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        for (EditOperation operation : operations) {
            System.out.println("Operation: " + Printer.print(operation));
        }
        assertEquals(1, operations.size());
        assertEquals(EditOperationType.INSERT, operations.get(0).getOperationType());
        assertEquals(5, operations.get(0).getPosition());
    }

    @Test
    public void testHirschbergsSubsitute() {
        int[] s1 = { 1, 2, 3, 4, 5 };
        int[] s2 = { 1, 2, 2, 1, 5 };
        // String s1 = "ABAB";
        // String s2 = "ABBAB";
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        assertEquals(1, operations.size());
        assertEquals(EditOperationType.SUBSTITUTE, operations.get(0).getOperationType());
        assertEquals(2, operations.get(0).getPosition());
        assertEquals(2, operations.get(0).getChars().size());
    }

    @Test
    public void testHirschbergsNoString() {
        int[] s1 = {};
        int[] s2 = { 1, 2, 3 };
        // String s1 = "ABAB";
        // String s2 = "ABBAB";
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        for (EditOperation op : operations) {
            System.out.println(Printer.print(op));
        }

    }

    @Test
    public void testHirschbergsEqualString() {
        int[] s1 = { 1, 2, 3 };
        int[] s2 = { 1, 2, 3 };
        // String s1 = "ABAB";
        // String s2 = "ABBAB";
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        assertEquals(0, operations.size());

    }

    @Test
    public void testHirschbergsBig() {
        int[] s1 = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 8, 9, 10, 11, 12, 13, 14, 8, 9, 10, 11, 12, 13, 14, 8,
                9, 10, 11, 12, 13, 14, 8, 9, 10, 11, 12, 13, 14, 15, 1 };
        int[] s2 = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 8, 9, 10, 11, 12, 13, 14, 8, 16, 10, 11, 12, 13, 14, 8,
                9, 10, 11, 12, 13, 14, 8, 9, 10, 11, 12, 13, 14, 15, 1 };

        // String s1 = "ABAB";
        // String s2 = "ABBAB";
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        for (EditOperation op : operations) {
            System.out.println(Printer.print(op));
        }
        assertEquals(1, operations.size());

    }

    @Test
    public void testHirschbergsAnotherBig() {
        int[] s1 = { 5, 6, 7, 8, 6, 9, };
        int[] s2 = { 7, 8, 6, 9, 6, 5, };

        // String s1 = "ABAB";
        // String s2 = "ABBAB";
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        for (EditOperation op : operations) {
            System.out.println(Printer.print(op));
        }

    }

    @Test
    public void testHirschbergsBug() {
        int[] s1 = { 2, 3, 4, 5, 6, 7, 8, 9, 7, 10, 11, 12, 13, 14, 8, 15, 8, 16, 17, 6, 18, 9, 8, 16, 19, 6, 11, 20,
                18, 11, 21, 7, 22, 15, 23, 24, 25, 24, 8, 21, 8, 15, 10, 24, 25, 24, 8, 21, 7, 26, 15, 7, 16, 27, 6, 28,
                9, 29, 16, 30, 6, 31, 16, 32, 6, 8, 11, 11, 11, 21, 33, 16, 34, 16, 35, 6, 22, 11, 21, 36, 37, 15, 38,
                36, 6, 31, 16, 32, 6, 22, 11, 9, 26, 11, 21, 37, 16, 39, 6, 40, 11, 21, 41, 16, 42, 6, 22, 9, 37, 11,
                21, 43, 1, 2, 3, 44, 45, 46, 45, 46, 47, 6, 44, 45, 46, 45, 46, 48, 11, 14, 44, 45, 46, 45, 46, 49, 15,
                38, 44, 45, 48, 45, 50, 46, 16, 19, 46, 45, 48, 16, 19, 46, 21, 51, 6, 44, 52, 15, 50, 21, 52, 53, 48,
                16, 19, 21, 52, 54, 11, 14, 51, 6, 44, 55, 15, 50, 21, 55, 53, 48, 45, 50, 46, 16, 19, 21, 55, 54, 11,
                14, 49, 45, 55, 46, 45, 52, 46, 15, 48, 45, 52, 46, 45, 55, 46, 21, 43, 43, 56, 49, 21, 43, 1 };
        int[] s2 = { 2, 3, 4, 5, 6, 7, 8, 9, 7, 10, 11, 12, 13, 14, 8, 15, 8, 16, 17, 6, 18, 9, 8, 16, 19, 6, 11, 20,
                18, 11, 21, 7, 22, 15, 23, 24, 25, 24, 8, 21, 8, 15, 10, 24, 25, 24, 8, 21, 7, 26, 15, 7, 16, 27, 6, 28,
                9, 29, 16, 30, 6, 31, 16, 32, 6, 8, 11, 11, 11, 21, 33, 16, 34, 16, 35, 6, 22, 11, 21, 36, 37, 15, 38,
                36, 6, 31, 16, 32, 6, 22, 11, 9, 26, 11, 21, 37, 16, 39, 6, 40, 11, 21, 41, 16, 42, 6, 22, 9, 37, 11,
                21, 43, 1 };
        HirschbergsAlgorithm hirschbergs = new HirschbergsAlgorithm(s1, s2);

        List<EditOperation> operations = hirschbergs.getOperations();
        for (EditOperation op : operations) {
            System.out.println(Printer.print(op));
        }
    }
}
