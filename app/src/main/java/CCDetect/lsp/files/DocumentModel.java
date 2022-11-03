package CCDetect.lsp.files;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import CCDetect.lsp.CodeClone;

public class DocumentModel {

    private final Path path;
    protected String text;
    private List<CodeClone> clones = new ArrayList<>();
    private boolean isOpen = false;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public DocumentModel(Path path, String text) {
        this.path = path;
        setText(text);
    }

    public String getText() {
        if (text == null) {
            setText(getDocumentContent());
        }
        return text;
    }

    public String getDocumentContent() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path), "utf-8"));
            String content = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            return content;
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }

        return null;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean hasText() {
        return text != null;
    }

    public void freeText() {
        setText(null);
    }

    public String getUri() {
        return path.toUri().toString();
    }

    public void setOpen(boolean value) {
        this.isOpen = value;
    }

    public boolean isOpen() {
        return isOpen;
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
