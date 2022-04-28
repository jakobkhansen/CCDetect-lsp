package CCDetect.lsp.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import CCDetect.lsp.CodeClone;

/**
 * DocumentIndex
 */
public class CompilaDocumentIndex implements DocumentIndex {

    private static final Logger LOGGER = Logger.getLogger(
        Logger.GLOBAL_LOGGER_NAME
    );
    private static final Logger FILE_LOGGER = Logger.getLogger(
        "CCFileStateLogger"
    );

    Map<String, DocumentModel> documents = Collections.synchronizedMap(
        new HashMap<>()
    );
    String rootUri;

    public CompilaDocumentIndex(String rootUri) {
        this.rootUri = rootUri;
    }

    @Override
    public void indexProject() {
        List<Path> filePaths = getFilePathsInProject();
        for (Path p : filePaths) {
            String documentContent = getDocumentContent(p);
            if (documentContent != null) {
                documents.put(
                    p.toUri().toString(),
                    new DocumentModel(p.toUri().toString(), documentContent)
                );
            }
        }
    }

    private List<Path> getFilePathsInProject() {
        List<Path> filePaths = new ArrayList<>();

        try {
            URI uri = new URI(rootUri);
            filePaths =
                Files
                    .find(
                        Paths.get(uri),
                        Integer.MAX_VALUE,
                        (filePath, fileAttr) ->
                            (
                                fileAttr.isRegularFile() ||
                                fileAttr.isDirectory()
                            ) &&
                            com.google.common.io.Files
                                .getFileExtension(filePath.toString())
                                .equals("ccdetect")
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePaths;
    }

    private String getDocumentContent(Path file) {
        try (
            BufferedReader reader = Files.newBufferedReader(
                file,
                Charset.forName("UTF-8")
            )
        ) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            return content;
        } catch (IOException ex) {
            ex.printStackTrace(); //handle an exception here
        }

        return null;
    }

    @Override
    public void updateDocument(String uri, DocumentModel updatedDocument) {
        documents.put(uri, updatedDocument);
    }

    @Override
    public DocumentModel getDocument(String uri) {
        return documents.get(uri);
    }

    @Override
    public void updateClones(List<CodeClone> clones) {
        for (DocumentModel doc : documents.values()) {
            doc.setClones(new ArrayList<>());
        }

        for (CodeClone clone : clones) {
            documents.get(clone.getUri()).addClone(clone);
        }

    }

    @Override
    public Iterator<DocumentModel> iterator() {
        return documents.values().iterator();
    }

}
