package CCDetect.lsp.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import CCDetect.lsp.codeactions.DeleteRangeActionProvider;
import CCDetect.lsp.codeactions.ExtractMethodActionProvider;

/**
 * CCTextDocumentService
 */
public class CCTextDocumentService implements TextDocumentService {
    // URI -> TextDocumentItem
    private final Map<String, DocumentModel> docs = Collections.synchronizedMap(new HashMap<>());

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
        CompletionParams position
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionItems = new ArrayList<>();
            try {
                // Sample Completion item for sayHello
                CompletionItem completionItem = new CompletionItem();
                // Define the text to be inserted in to the file if the completion item is selected.
                completionItem.setInsertText(
                    "sayHello() {\n    print(\"hello\")\n}"
                );
                // Set the label that shows when the completion drop down appears in the Editor.
                completionItem.setLabel("sayHello()");
                // Set the completion kind. This is a snippet.
                // That means it replace character which trigger the completion and
                // replace it with what defined in inserted text.
                completionItem.setKind(CompletionItemKind.Snippet);
                // This will set the details for the snippet code which will help user to
                // understand what this completion item is.
                completionItem.setDetail(
                    "sayHello()\n this will say hello to the people"
                );

                // Add the sample completion item to the list.
                completionItems.add(completionItem);
            } catch (Exception e) {}

            // Return the list of completion items.
            return Either.forLeft(completionItems);
        });
    }


    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
        CodeActionParams params
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
            try {
                DocumentModel document = docs.get(params.getTextDocument().getUri());

                DeleteRangeActionProvider deleteRangeProvider = new DeleteRangeActionProvider(params, document);
                ExtractMethodActionProvider extractProvider = new ExtractMethodActionProvider(params, document);

                CodeAction deleteRangeAction = deleteRangeProvider.getCodeAction();
                CodeAction extractMethodAction = extractProvider.getCodeAction();

                codeActions.add(Either.forRight(deleteRangeAction));
                codeActions.add(Either.forRight(extractMethodAction));
            } catch (Exception e) {}

            return codeActions;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        DocumentModel model = new DocumentModel(params.getTextDocument().getText());
        docs.put(params.getTextDocument().getUri(), model);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        // TODO Auto-generated method stub
        DocumentModel model = new DocumentModel(params.getContentChanges().get(0).getText());
        this.docs.put(params.getTextDocument().getUri(), model);
        

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO Auto-generated method stub

    }
}
