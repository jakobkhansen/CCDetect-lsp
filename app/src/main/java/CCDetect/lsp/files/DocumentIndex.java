package CCDetect.lsp.files;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;

/**
 * DocumentIndex
 */
public interface DocumentIndex<T extends DocumentModel> extends Iterable<T> {

    void indexProject();

    void updateDocument(String uri, T updatedDocument);

    void updateDocument(String uri, String updatedContent);

    // Used when you want to keep original DocumentModel instance
    default void updateDocument(String uri, Range range, String updatedContent) {
    };

    void markFileDeleted(String uri);

    void deleteFile(String uri);

    T getDocument(String uri);

    boolean containsDocument(String uri);

    int size();

    default void updateClones(List<CodeClone> clones) {

        for (DocumentModel doc : this) {
            doc.setClones(new ArrayList<>());
        }

        for (CodeClone clone : clones) {
            this.getDocument(clone.getUri()).addClone(clone);
        }

    };

}
