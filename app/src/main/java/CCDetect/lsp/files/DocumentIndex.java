package CCDetect.lsp.files;

import java.util.List;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;

/**
 * DocumentIndex
 */
public interface DocumentIndex<T extends DocumentModel> extends Iterable<T> {

    void indexProject();

    void updateDocument(String uri, T updatedDocument);

    // Used when you want to keep original DocumentModel instance
    default void updateDocument(String uri, Range range, String updatedContent) {
    };

    DocumentModel getDocument(String uri);

    boolean containsDocument(String uri);

    void updateClones(List<CodeClone> clones);

}
