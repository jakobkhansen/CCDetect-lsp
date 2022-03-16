package CCDetect.lsp.codeactions;

import java.util.logging.Logger;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.ShowDocumentParams;

import CCDetect.lsp.server.CCLanguageServer;

/**
 * CommandHandler
 */
public class CommandHandler {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final ExecuteCommandParams command;

    public CommandHandler(ExecuteCommandParams command) {
        this.command = command;
    }

    public void execute() {
        switch (command.getCommand()) {
            case "showDocument":
                executeShowDocument();
                break;

            default:
                break;
        }
    }

    private void executeShowDocument() {
        LOGGER.info("executeShowDocument");
        ShowDocumentParams params = new ShowDocumentParams("file:///home/jakob/CompilaServerTest/test02.cmp");
        params.setTakeFocus(true);
        CCLanguageServer.getInstance().client.showDocument(params);
    }
}
