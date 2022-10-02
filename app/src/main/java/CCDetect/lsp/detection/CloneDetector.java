package CCDetect.lsp.detection;

import java.util.List;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;

/**
 * CloneDetector
 */
public interface CloneDetector {
    List<CodeClone> getClones();

    void onIndexChange(DocumentIndex<DocumentModel> index);

}
