package CCDetect.lsp.server;

import CCDetect.lsp.codeactions.CommandHandler;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * CCWorkspaceService
 */
public class CCWorkspaceService implements WorkspaceService {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

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
        // TODO Auto-generated method stub
    }
}
