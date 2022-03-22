package CCDetect.lsp.codeactions;

import CCDetect.lsp.server.CCLanguageServer;
import CCDetect.lsp.utils.JSONUtility;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowDocumentParams;

/**
 * CommandHandler
 */
public class CommandHandler {

    private static final Logger LOGGER = Logger.getLogger(
        Logger.GLOBAL_LOGGER_NAME
    );
    private final ExecuteCommandParams command;

    public CommandHandler(ExecuteCommandParams command) {
        this.command = command;
    }

    public void execute() {
        LOGGER.info(command.getCommand());
        switch (command.getCommand()) {
            case "showDocument":
                executeShowDocument(command);
                break;
            default:
                break;
        }
    }

    private void executeShowDocument(ExecuteCommandParams command) {
        List<Object> args = command
            .getArguments()
            .stream()
            .map(element -> {
                return JSONUtility.toModel(element, Object.class);
            })
            .collect(Collectors.toList());

        String uri = JSONUtility.toModel(args.get(0), String.class);
        Range range = JSONUtility.toModel(args.get(1), Range.class);

        ShowDocumentParams params = new ShowDocumentParams(uri);
        params.setSelection(range);
        params.setTakeFocus(true);

        CCLanguageServer.getInstance().client.showDocument(params);
    }
}
