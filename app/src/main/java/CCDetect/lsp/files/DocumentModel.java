package CCDetect.lsp.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;

public class DocumentModel {

    private final String uri;
    private String text;
    private final List<DocumentLine> lines = new ArrayList<>();
    private List<CodeClone> clones = new ArrayList<>();

    public DocumentModel(String uri, String text) {
        this.uri = uri;
        this.text = text;
        try (
                Reader r = new StringReader(text);
                BufferedReader reader = new BufferedReader(r);) {
            String lineText;
            int lineNumber = 0;
            while ((lineText = reader.readLine()) != null) {
                DocumentLine line = new DocumentLine(uri, lineNumber, lineText);
                lines.add(line);
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getText() {
        return text;
    }

    public List<DocumentLine> getLines() {
        return lines;
    }

    public String getUri() {
        return uri;
    }

    public List<DocumentLine> getLinesInRange(Range range) {
        int startLine = range.getStart().getLine();
        int endLine = range.getEnd().getLine();

        return lines.subList(startLine, endLine + 1);
    }

    public String getLineTextInRange(Range range) {
        int startLine = range.getStart().getLine();
        int endLine = range.getEnd().getLine();

        List<DocumentLine> rangeLines = lines.subList(startLine, endLine + 1);
        StringBuilder linesString = new StringBuilder();

        for (DocumentLine line : rangeLines) {
            linesString.append("\n" + line.toString());
        }

        return linesString.toString();
    }

    public void setClones(List<CodeClone> clones) {
        this.clones = clones;
    }

    public void addClone(CodeClone clone) {
        this.clones.add(clone);
    }

    public List<CodeClone> getClones() {
        return this.clones;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (DocumentLine line : lines) {
            out.append(line + "\n");
        }

        return out.toString();
    }
}
