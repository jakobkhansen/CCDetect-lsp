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

    public static class DocumentLine {

        final int line;
        final String text;

        public DocumentLine(int line, String text) {
            this.line = line;
            this.text = text;
        }

        public String getInRange(int start, int end) {
            return text.substring(start, end);
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final List<DocumentLine> lines = new ArrayList<>();
    private List<CodeClone> clones;



    public DocumentModel(String text) {
        try (
            Reader r = new StringReader(text);
            BufferedReader reader = new BufferedReader(r);
        ) {
            String lineText;
            int lineNumber = 0;
            while ((lineText = reader.readLine()) != null) {
                DocumentLine line = new DocumentLine(lineNumber, lineText);
                lines.add(line);
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DocumentLine> getLines() {
        return lines;
    }

    public List<DocumentLine> getLinesInRange(Range range) {
        int startLine = range.getStart().getLine();
        int endLine = range.getEnd().getLine();

        return lines.subList(startLine, endLine+1);
    }

    public String getLineTextInRange(Range range) {
        int startLine = range.getStart().getLine();
        int endLine = range.getEnd().getLine();

        List<DocumentLine> rangeLines = lines.subList(startLine, endLine+1);
        StringBuilder linesString = new StringBuilder();

        linesString.append("\nprocedure extracted()");
        linesString.append("\nbegin");
        for (DocumentLine line : rangeLines) {
            linesString.append("\n" + line.toString());
        }
        linesString.append("\nend");


        return linesString.toString();
    }
}
