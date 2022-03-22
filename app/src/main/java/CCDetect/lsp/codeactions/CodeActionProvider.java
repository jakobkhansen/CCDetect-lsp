package CCDetect.lsp.codeactions;

import CCDetect.lsp.files.DocumentModel;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * CodeActionProvider
 */
public class CodeActionProvider {

    public static List<Either<Command, CodeAction>> createCodeActions(
        DocumentModel document,
        Range range
    ) {
        List<Either<Command, CodeAction>> actions = new ArrayList<>();

        CodeAction jumpAction = CodeCloneJumpProvider.createJumpAction(
            document,
            range
        );

        if (jumpAction != null) {
            actions.add(Either.forRight(jumpAction));
        }

        return actions;
    }
}
