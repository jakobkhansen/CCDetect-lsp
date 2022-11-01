package CCDetect.lsp.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;

public class DocumentModel {

    private final String uri;
    protected String text;
    private List<CodeClone> clones = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public DocumentModel(String uri, String text) {
        this.uri = uri;
        setText(text);
        try (
                Reader r = new StringReader(text);
                BufferedReader reader = new BufferedReader(r);) {
            String lineText;
            int lineNumber = 0;
            while ((lineText = reader.readLine()) != null) {
                lineNumber++;
            }
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void freeText() {
        setText(null);
    }

    public String getUri() {
        return uri;
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
        return text;
    }
}
