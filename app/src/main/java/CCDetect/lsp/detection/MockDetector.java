package CCDetect.lsp.detection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;

/**
 * MockDetector
 */
public class MockDetector implements CloneDetector {
    List<CodeClone> clones = new ArrayList<>();

    @Override
    public List<CodeClone> getClones() {
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex index) {
        clones = new ArrayList<>();
        CodeClone clone1 = new CodeClone("file:///home/jakob/Documents/CompilaServerTest/test02.cmp", new Range(new Position(2,0), new Position(3,0)));
        CodeClone clone2 = new CodeClone("file:///home/jakob/Documents/CompilaServerTest/test01.cmp", new Range(new Position(6,0), new Position(7,0)));
        clone1.addMatchingClone(clone2);
        clone2.addMatchingClone(clone1);

        clones.add(clone1);
        clones.add(clone2);
    }
}
