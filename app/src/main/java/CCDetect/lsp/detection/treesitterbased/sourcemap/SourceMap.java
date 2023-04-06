package CCDetect.lsp.detection.treesitterbased.sourcemap;

import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;

/**
 * SourceMap
 */
public interface SourceMap {
    TokenSource getSource(int index);

    TreesitterDocumentModel[] getDocuments();
}
