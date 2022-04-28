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


    public static List<CodeAction> createJumpActions(DocumentModel document, Range range) {
        List<CodeAction> actions = new ArrayList<>();
        for (CodeClone clone : document.getClones()) {
            if (clone.isInRange(range)) {
                for (CodeClone otherClone : clone.getMatches()) {
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

                actions.add(action);
                }
            }
        }

        return actions;
    }
}
