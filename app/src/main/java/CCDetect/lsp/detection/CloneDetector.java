package CCDetect.lsp.detection;

import java.util.List;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;

/**
 * CloneDetector
 */
public interface CloneDetector {
    List<CodeClone> getClones();

    void onIndexChange(DocumentIndex index);

}
