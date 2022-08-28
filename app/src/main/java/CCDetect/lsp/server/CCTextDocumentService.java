package CCDetect.lsp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
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
import CCDetect.lsp.detection.tokenbased.HybridJavaDetector;
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
        detector = new HybridJavaDetector();
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionItems = new ArrayList<>();
            try {
                // Sample Completion item for sayHello
                CompletionItem completionItem = new CompletionItem();
                // Define the text to be inserted in to the file if the completion item is
                // selected.
                completionItem.setInsertText(
                        "sayHello() {\n    print(\"hello\")\n}");
                // Set the label that shows when the completion drop down appears in the Editor.
                completionItem.setLabel("sayHello()");
                // Set the completion kind. This is a snippet.
                // That means it replace character which trigger the completion and
                // replace it with what defined in inserted text.
                completionItem.setKind(CompletionItemKind.Snippet);
                // This will set the details for the snippet code which will help user to
                // understand what this completion item is.
                completionItem.setDetail(
                        "sayHello()\n this will say hello to the people");

                // Add the sample completion item to the list.
                completionItems.add(completionItem);
            } catch (Exception e) {
            }

            // Return the list of completion items.
            return Either.forLeft(completionItems);
        });
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
        DocumentModel model = new DocumentModel(params.getTextDocument().getUri(), params.getTextDocument().getText());
        index.updateDocument(params.getTextDocument().getUri(), model);

        updateClones();
        updateDiagnostics();
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {

        TextDocumentContentChangeEvent lastChange = params.getContentChanges()
                .get(params.getContentChanges().size() - 1);
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
}
