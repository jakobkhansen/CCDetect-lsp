package CCDetect.lsp.server;

import CCDetect.lsp.codeactions.CommandHandler;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CreateFilesParams;
import org.eclipse.lsp4j.DeleteFilesParams;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileDelete;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * CCWorkspaceService
 */
public class CCWorkspaceService implements WorkspaceService {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    private DocumentIndex<TreesitterDocumentModel> index;
    private CloneDetector<TreesitterDocumentModel> detector;

    public void initialize(DocumentIndex<TreesitterDocumentModel> index,
            CloneDetector<TreesitterDocumentModel> detector) {
        this.index = index;
        this.detector = detector;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public CompletableFuture<Object> executeCommand(
            ExecuteCommandParams params) {
        LOGGER.info("executeCommand");
        return CompletableFuture.supplyAsync(() -> {
            CommandHandler handler = new CommandHandler(params);
            handler.execute();

            return null;
        });
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        LOGGER.info("didChangeWatchedFiles");
        for (FileEvent event : params.getChanges()) {
            switch (event.getType()) {
                case Deleted:
                    LOGGER.info("Deleted file " + event.getUri());
                    index.markFileDeleted(event.getUri());
                    detector.onIndexChange(index);
                    index.deleteFile(event.getUri());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void didCreateFiles(CreateFilesParams params) {
        WorkspaceService.super.didCreateFiles(params);
    }

    @Override
    public void didDeleteFiles(DeleteFilesParams params) {
        WorkspaceService.super.didDeleteFiles(params);
        List<FileDelete> files = params.getFiles();
        for (FileDelete file : files) {
            LOGGER.info("Deleted file " + file.getUri());
            index.markFileDeleted(file.getUri());
        }
        detector.onIndexChange(index);
    }
}
