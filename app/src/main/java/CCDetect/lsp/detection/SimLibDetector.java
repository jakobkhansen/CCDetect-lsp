package CCDetect.lsp.detection;

import java.util.ArrayList;
import java.util.List;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;

/**
 * SimLibDetector
 */
public class SimLibDetector implements CloneDetector {
    List<CodeClone> clones = new ArrayList<>();

    @Override
    public List<CodeClone> getClones() {
        // TODO Auto-generated method stub
        return clones;
    }

    @Override
    public void onIndexChange(DocumentIndex index) {
        // TODO Auto-generated method stub
        
        
    }

    
}
