package CCDetect.lsp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.codeactions.CodeCloneJumpProvider;
import CCDetect.lsp.codeactions.DeleteRangeActionProvider;
import CCDetect.lsp.codeactions.ExtractMethodActionProvider;
import CCDetect.lsp.codeactions.JumpToDocumentActionProvider;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.detection.MockDetector;
import CCDetect.lsp.diagnostics.DiagnosticsPublisher;
import CCDetect.lsp.files.CompilaDocumentIndex;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;

/**
 * CCTextDocumentService
 */
public class CCTextDocumentService implements TextDocumentService {
    // URI -> TextDocumentItem
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final static Logger FILE_LOGGER = Logger.getLogger("CCFileStateLogger");
    private DocumentIndex index;
    private CloneDetector detector;

    public void initialize(String rootUri) {
        createIndex(rootUri);
        createDetector();
    }

    public void createIndex(String rootUri) {
        index = new CompilaDocumentIndex(rootUri);
        index.indexProject();
    }

    public void createDetector() {
        detector = new MockDetector();
    }


    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
        CodeActionParams params
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
            try {
                LOGGER.info("codeAction");
                DocumentModel document = index.getDocument(params.getTextDocument().getUri());
                Range range = params.getRange();

                CodeAction jumpAction = CodeCloneJumpProvider.createJumpAction(document, range);

                if (jumpAction != null) {
                    codeActions.add(Either.forRight(jumpAction));
                }
            } catch (Exception e) {}

            return codeActions;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        DocumentModel model = new DocumentModel(params.getTextDocument().getUri(), params.getTextDocument().getText());
        index.updateDocument(params.getTextDocument().getUri(), model);

        updateClones();
        updateDiagnostics();
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {

        TextDocumentContentChangeEvent lastChange = params.getContentChanges().get(params.getContentChanges().size()-1);
        DocumentModel model = new DocumentModel(params.getTextDocument().getUri(), lastChange.getText());
        index.updateDocument(params.getTextDocument().getUri(), model);
        
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

    // TODO Remove
    public void testDiagnostic(String uri) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        Range range1 = new Range(new Position(0, 0), new Position(0,11));
        Range range2 = new Range(new Position(1, 0), new Position(1,5));
        Diagnostic diagnostic1 = new Diagnostic(range1, "This is a diagnostic");
        Diagnostic diagnostic2 = new Diagnostic(range2, "This is a warning", DiagnosticSeverity.Warning, "source");
        diagnostics.add(diagnostic1);
        diagnostics.add(diagnostic2);
        CompletableFuture.runAsync(() ->
			CCLanguageServer.getInstance().client.publishDiagnostics(
				new PublishDiagnosticsParams(uri, diagnostics)
			)
		);

    }
}
