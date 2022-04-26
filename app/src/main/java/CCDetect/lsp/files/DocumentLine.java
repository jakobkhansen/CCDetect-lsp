package CCDetect.lsp.files;

public class DocumentLine {

    public final String uri;
    public final int line;
    public final String text;

    public DocumentLine(String uri, int line, String text) {
        this.uri = uri;
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
