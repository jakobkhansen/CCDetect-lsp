package CCDetect.lsp.files;

import java.util.List;

import CCDetect.lsp.CodeClone;

/**
 * DocumentIndex
 */
public interface DocumentIndex {

    void indexProject();
    DocumentModel getDocument(String uri);
    void updateClones(List<CodeClone> clones);
}
