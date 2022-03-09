package CCDetect.lsp.codeactions;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import CCDetect.lsp.server.DocumentModel;
import CCDetect.lsp.server.DocumentModel.DocumentLine;

public class ExtractMethodActionProvider {
    CodeActionParams params;
    DocumentModel document;

    public ExtractMethodActionProvider(CodeActionParams params, DocumentModel document) {
        this.params = params;
        this.document = document;
    }


    public CodeAction getCodeAction() {
        CodeAction action = new CodeAction("Extract Method");
        action.getCommand();
        action.setEdit(getEdit());

        return action;
    }

    private WorkspaceEdit getEdit() {

        WorkspaceEdit edit = new WorkspaceEdit();

        HashMap<String, List<TextEdit>> changes = new HashMap<>();

        List<TextEdit> edits = new ArrayList<>();

        TextEdit removeEdit = new TextEdit();


        removeEdit.setNewText("");
        removeEdit.setRange(params.getRange());



        // Place at bottom
        TextEdit placeEdit = new TextEdit();
        placeEdit.setNewText(document.getLineTextInRange(params.getRange()));
        Position lastLine = new Position(document.getLines().size(), 0);

        placeEdit.setRange(new Range(lastLine, lastLine));

        edits.add(removeEdit);
        edits.add(placeEdit);

        changes.put(params.getTextDocument().getUri(), edits);
        edit.setChanges(changes);

        return edit;
    }
}
