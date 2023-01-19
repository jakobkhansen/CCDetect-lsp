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
}
