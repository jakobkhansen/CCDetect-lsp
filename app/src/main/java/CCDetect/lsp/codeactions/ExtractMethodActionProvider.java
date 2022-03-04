package CCDetect.lsp.codeactions;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

public class ExtractMethodActionProvider {
    public static CodeAction getCodeAction(CodeActionParams params) {
        System.err.println(params.getTextDocument().getUri());
        CodeAction action = new CodeAction("Extract Method");
        action.getCommand();
        action.setEdit(getEdit(params));

        return action;
    }

    private static WorkspaceEdit getEdit(CodeActionParams params) {
        WorkspaceEdit edit = new WorkspaceEdit();

        HashMap<String, List<TextEdit>> changes = new HashMap<>();

        URL url = null;
        InputStream stream = null;
        Scanner scanner = null;
        try {
            url = new URL(params.getTextDocument().getUri()); 
            stream = url.openStream();
            scanner = new Scanner(stream);

        } catch (Exception e) {
            //TODO: handle exception
        }
        String documentContent = scanner.nextLine();
        System.err.println(documentContent);

        List<TextEdit> edits = new ArrayList<>();

        TextEdit textEdit = new TextEdit();


        textEdit.setNewText("");
        textEdit.setRange(params.getRange());
        edits.add(textEdit);

        changes.put(params.getTextDocument().getUri(), edits);
        edit.setChanges(changes);

        return edit;
    }
}
