package CCDetect.lsp.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.codeactions.CodeActionProvider;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.diagnostics.DiagnosticsPublisher;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;

/**
 * CCTextDocumentService
 */
public class CCTextDocumentService implements TextDocumentService {
    // URI -> TextDocumentItem
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static Logger FILE_LOGGER = Logger.getLogger("CCFileStateLogger");
    private DocumentIndex<TreesitterDocumentModel> index;
    private CloneDetector<TreesitterDocumentModel> detector;

    public void initialize(String rootUri) {
        createIndex(rootUri);
        createDetector();
    }

    public void createIndex(String rootUri) {
        index = new TreesitterDocumentIndex(rootUri);
        index.indexProject();
    }

    public void createDetector() {
        detector = new TreesitterDetector();
        detector.onIndexChange(index);
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
            CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            DocumentModel document = index.getDocument(params.getTextDocument().getUri());
            Range range = params.getRange();

            return CodeActionProvider.createCodeActions(document, range);
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOGGER.info("didOpen");

        TreesitterDocumentModel model = new TreesitterDocumentModel(params.getTextDocument().getUri(),
                params.getTextDocument().getText());

        index.updateDocument(params.getTextDocument().getUri(), model);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LOGGER.info("didChange");
        String uri = params.getTextDocument().getUri();

        if (!index.containsDocument(uri)) {
            LOGGER.info("Unknown document");
            return;
        }

        List<TextDocumentContentChangeEvent> changes = params.getContentChanges();

        for (TextDocumentContentChangeEvent change : changes) {
            LOGGER.info("Change: " + change.getText());
            index.updateDocument(uri, change.getRange(), change.getText());
        }

        updateClones();
        updateDiagnostics();
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LOGGER.info("didRemove");
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LOGGER.info("didSave");
    }

    public void updateClones() {
        detector.onIndexChange(index);
        List<CodeClone> currentClones = detector.getClones();
        index.updateClones(currentClones);
    }

    public void updateDiagnostics() {
        DiagnosticsPublisher.publishCloneDiagnosticsFromIndex(index);
    }
}
