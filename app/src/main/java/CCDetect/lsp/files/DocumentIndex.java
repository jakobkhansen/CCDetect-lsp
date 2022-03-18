package CCDetect.lsp.files;

import java.util.List;

import CCDetect.lsp.CodeClone;

/**
 * DocumentIndex
 */
public interface DocumentIndex extends Iterable<DocumentModel> {

    void indexProject();
    void updateDocument(String uri, DocumentModel updatedDocument);
    DocumentModel getDocument(String uri);
    void updateClones(List<CodeClone> clones);
}
