package CCDetect.lsp.codeactions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.utils.JSONUtility;

/**
 * CodeCloneJumpProvider
 */
public class CodeCloneJumpProvider {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static CodeAction createJumpAction(DocumentModel document, Range range) {
        for (CodeClone clone : document.getClones()) {
            LOGGER.info("Clone in doc");
            LOGGER.info("Clone range: " + clone.getRange().toString());

            if (clone.isInRange(range)) {
                CodeClone otherClone = clone.getPairMatch();
                CodeAction action = new CodeAction("Jump to matching clone");

                Command command = new Command("showDocument", "showDocument");
                List<Object> arguments = new ArrayList<>();

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();

                String rangeJson = gson.toJson(otherClone.getRange());

                arguments.add(otherClone.getUri());
                arguments.add(rangeJson);
                command.setArguments(arguments);
                action.setCommand(command);
                return action;
            }
        }

        return null;
    }
}
