package CCDetect.lsp.server;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
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
import CCDetect.lsp.files.fileiterators.GitProjectIterator;
import CCDetect.lsp.utils.Timer;

/**
 * CCTextDocumentService
 */
public class CCTextDocumentService implements TextDocumentService {
    // URI -> TextDocumentItem
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private DocumentIndex<TreesitterDocumentModel> index;
    private CloneDetector<TreesitterDocumentModel> detector;

    public void initialize(DocumentIndex<TreesitterDocumentModel> index,
            CloneDetector<TreesitterDocumentModel> detector) {
        Timer timer = new Timer();
        timer.start();

        this.index = index;
        this.detector = detector;

        findClones();
        updateDiagnostics();
        timer.stop();
        timer.log("Initialize time");
    }

    public void createIndex(String rootUri) {
        Configuration config = Configuration.getInstance();
        index = new TreesitterDocumentIndex(rootUri, new GitProjectIterator(rootUri, config.getLanguage()));
        index.indexProject();
    }

    public void createDetector() {
        detector = new TreesitterDetector();
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(
            CodeActionParams params) {
        LOGGER.info("codeActions published");
        return CompletableFuture.supplyAsync(() -> {

            String uri = params.getTextDocument().getUri();

            DocumentModel document = index.getDocument(uri);
            Range range = params.getRange();

            return CodeActionProvider.createCodeActions(document, range);
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOGGER.info("didOpen");
        String uri = params.getTextDocument().getUri();
        TreesitterDocumentModel model;
        if (index.containsDocument(uri)) {
            LOGGER.info("Opened already indexed document");
            model = index.getDocument(uri);
        } else {

            String uriFormatted = uri.substring(7);

            Path path = Paths.get(uriFormatted);

            model = new TreesitterDocumentModel(path, null);
        }
        model.setOpen(true);
        model.buildTree();

        index.updateDocument(uri, model);
        updateClones();
        updateDiagnostics();
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Timer timer = new Timer();
        timer.start();
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

        if (!Configuration.getInstance().shouldUpdateOnSave()) {
            findClones();
            updateDiagnostics();
        }

        timer.stop();
        timer.log("didChange total time");
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        LOGGER.info("didClose");
        DocumentModel model = index.getDocument(uri);
        model.setOpen(false);
        updateDiagnostics();
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        LOGGER.info("didSave");
        Timer timer = new Timer();
        timer.start();

        if (Configuration.getInstance().shouldUpdateOnSave()) {
            findClones();
            updateDiagnostics();
        }

        timer.stop();
        timer.log("didSave total time");
    }

    public void findClones() {
        detector.onIndexChange(index);
        List<CodeClone> currentClones = detector.getClones();
        index.updateClones(currentClones);
    }

    public void updateClones() {
        List<CodeClone> currentClones = detector.getClones();
        index.updateClones(currentClones);
    }

    public void updateDiagnostics() {
        DiagnosticsPublisher.publishCloneDiagnosticsFromIndex(index);
    }

}
