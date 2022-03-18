package CCDetect.lsp.codeactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import CCDetect.lsp.files.DocumentModel;

/**
 * JumpToDocumentActionProvider
 */
public class JumpToDocumentActionProvider {

    CodeActionParams params;
    DocumentModel document;

    public JumpToDocumentActionProvider(CodeActionParams params, DocumentModel document) {
        this.params = params;
        this.document = document;
    }


    public CodeAction getCodeAction() {
        CodeAction action = new CodeAction("Jump to document");
        action.setCommand(new Command("Hello", "showDocument"));

        return action;
    }
}
