package CCDetect.lsp;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import CCDetect.lsp.datastructures.editdistance.EditOperationsCalculator;
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
}
