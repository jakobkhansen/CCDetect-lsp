package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import CCDetect.lsp.datastructures.editdistance.EditOperationsCalculator;
import CCDetect.lsp.utils.Printer;
import CCDetect.lsp.datastructures.editdistance.EditOperation;
import CCDetect.lsp.datastructures.editdistance.EditOperationType;

public class TestEditOperationsCalculator {
    @Test
    public void testSimpleEditOperation() {
        int[] s1 = { 1, 2, 3, 4, 5 };
        int[] s2 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        List<EditOperation> operations = EditOperationsCalculator.findEditOperations(s1, s2);

        assertEquals(1, operations.size());
        assertEquals(EditOperationType.INSERT, operations.get(0).getOperationType());
        assertEquals(5, operations.get(0).getPosition());
    }

    @Test
    public void testInsertInMiddleOperation() {
        int[] s1 = { 1, 2, 3, };
        int[] s2 = { 1, 2, 3, 1, 2, 3 };

        List<EditOperation> operations = EditOperationsCalculator.findEditOperations(s1, s2);

        assertEquals(1, operations.size());
        assertEquals(EditOperationType.INSERT, operations.get(0).getOperationType());
        assertEquals(3, operations.get(0).getPosition());
    }

    @Test
    public void testSimpleSubstitute() {
        int[] s1 = { 1, 2, 3, 4, 5 };
        int[] s2 = { 1, 2, 2, 1, 5 };

        List<EditOperation> operations = EditOperationsCalculator.findEditOperations(s1, s2);

        assertEquals(1, operations.size());
        assertEquals(EditOperationType.SUBSTITUTE, operations.get(0).getOperationType());
        System.out.println(operations.get(0).getPosition());
        for (EditOperation op : operations) {
            System.out.println(Printer.print(op));
        }
        assertEquals(2, operations.get(0).getPosition());
        assertEquals(2, operations.get(0).getChars().size());
    }
}
