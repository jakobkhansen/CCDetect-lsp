package CCDetect.lsp.codeactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import CCDetect.lsp.server.DocumentModel;
import CCDetect.lsp.server.DocumentModel.DocumentLine;

public class DeleteRangeActionProvider {
    CodeActionParams params;
    DocumentModel document;

    public DeleteRangeActionProvider(CodeActionParams params, DocumentModel document) {
        this.params = params;
        this.document = document;
    }

    public CodeAction getCodeAction() {
        CodeAction action = new CodeAction("Delete range");
        action.getCommand();
        action.setEdit(getEdit());

        return action;
    }

    private WorkspaceEdit getEdit() {
        WorkspaceEdit edit = new WorkspaceEdit();

        HashMap<String, List<TextEdit>> changes = new HashMap<>();

        List<TextEdit> edits = new ArrayList<>();

        TextEdit textEdit = new TextEdit();

        textEdit.setRange(params.getRange());
        textEdit.setNewText("");
        edits.add(textEdit);

        changes.put(params.getTextDocument().getUri(), edits);
        edit.setChanges(changes);

        return edit;
    }
}
