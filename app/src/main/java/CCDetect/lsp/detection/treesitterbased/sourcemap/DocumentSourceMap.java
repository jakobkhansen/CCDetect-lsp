package CCDetect.lsp.detection.treesitterbased.sourcemap;

import java.util.logging.Logger;

import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;

/**
 * DocumentSourceMap
 */
public class DocumentSourceMap implements SourceMap {
    DocumentIndex<TreesitterDocumentModel> documentIndex;
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public DocumentSourceMap(DocumentIndex<TreesitterDocumentModel> documentIndex) {
        this.documentIndex = documentIndex;

    }

    @Override
    public TokenSource getSource(int index) {
        for (TreesitterDocumentModel doc : documentIndex) {
            if (isInRange(doc, index)) {
                int current = index - doc.getFingerprintStart();
                for (Fingerprint fingerprint : doc.getFingerprint()) {
                    if (current < fingerprint.getRanges().length) {
                        return new TokenSource(doc.getUri(), fingerprint.getRanges()[current]);
                    }
                    current -= fingerprint.getRanges().length;
                }
            }
        }
        return null;
    }

    private boolean isInRange(TreesitterDocumentModel model, int index) {
        return model.getFingerprintStart() <= index && index <= model.getFingerprintEnd();
    }

}
