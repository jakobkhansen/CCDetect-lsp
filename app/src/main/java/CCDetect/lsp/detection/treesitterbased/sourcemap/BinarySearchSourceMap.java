package CCDetect.lsp.detection.treesitterbased.sourcemap;

import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import CCDetect.lsp.detection.treesitterbased.fingerprint.Fingerprint;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.utils.Printer;

/**
 * BinarySearchSourceMap
 */
public class BinarySearchSourceMap implements SourceMap {
    public TreesitterDocumentModel[] documents;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public BinarySearchSourceMap(DocumentIndex<TreesitterDocumentModel> index) {
        documents = StreamSupport.stream(index.spliterator(), false).sorted(new Comparator<TreesitterDocumentModel>() {
            @Override
            public int compare(TreesitterDocumentModel arg0, TreesitterDocumentModel arg1) {
                return arg0.getFingerprintStart() - arg1.getFingerprintEnd();
            }

        }).toArray(TreesitterDocumentModel[]::new);
    }

    @Override
    public TokenSource getSource(int index) {
        int left = 0;
        int right = documents.length - 1;

        while (left <= right) {
            int mid = (left + right) / 2;

            if (documents[mid].getFingerprintEnd() < index) {
                left = mid + 1;
            } else if (documents[mid].getFingerprintStart() > index) {
                right = mid - 1;
            } else {
                TreesitterDocumentModel model = documents[mid];
                int current = index - model.getFingerprintStart();

                for (Fingerprint fingerprint : model.getFingerprint()) {
                    if (current < fingerprint.getRanges().length) {
                        return new TokenSource(model.getUri(), fingerprint.getRanges()[current]);
                    }
                    current -= fingerprint.getRanges().length;
                }
                Fingerprint last = model.getFingerprint().get(model.getFingerprint().size() - 1);
                return new TokenSource(model.getUri(), last.getRanges()[last.getRanges().length - 1]);
            }
        }

        return null;
    }

    @Override
    public TreesitterDocumentModel[] getDocuments() {
        return documents;
    }
}
