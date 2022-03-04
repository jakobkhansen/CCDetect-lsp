package CCDetect.lsp.codeactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

public class DeleteRangeActionProvider {
    public static CodeAction getCodeAction(CodeActionParams params) {
        System.err.println(params.getTextDocument().getUri());
        CodeAction action = new CodeAction("Delete range");
        action.getCommand();
        action.setEdit(getEdit(params));

        return action;
    }

    private static WorkspaceEdit getEdit(CodeActionParams params) {
        WorkspaceEdit edit = new WorkspaceEdit();

        HashMap<String, List<TextEdit>> changes = new HashMap<>();

        List<TextEdit> edits = new ArrayList<>();

        TextEdit textEdit = new TextEdit();

        System.err.println(params.getRange().getStart().toString());
        System.err.println(params.getRange().getEnd().toString());
        textEdit.setRange(params.getRange());
        textEdit.setNewText("");
        edits.add(textEdit);

        changes.put(params.getTextDocument().getUri(), edits);
        edit.setChanges(changes);

        return edit;
    }
}
