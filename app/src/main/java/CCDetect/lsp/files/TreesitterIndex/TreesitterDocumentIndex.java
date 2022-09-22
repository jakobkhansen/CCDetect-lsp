package CCDetect.lsp.files.TreesitterIndex;

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

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;

/**
 * DocumentIndex
 */
public class TreesitterDocumentIndex implements DocumentIndex<TreesitterDocumentModel> {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    Map<String, TreesitterDocumentModel> documents = Collections.synchronizedMap(
            new HashMap<>());
    String rootUri;

    public TreesitterDocumentIndex(String rootUri) {
        this.rootUri = rootUri;
    }

    @Override
    public void indexProject() {
        double t1 = System.nanoTime();
        List<Path> filePaths = getFilePathsInProject();
        for (Path p : filePaths) {
            String documentContent = getDocumentContent(p);
            if (documentContent != null) {
                TreesitterDocumentModel model = new TreesitterDocumentModel(p.toUri().toString(), documentContent);
                updateDocument(p.toUri().toString(), model);
            }
        }
        double t2 = System.nanoTime();
        double runtimeInMs = (t2 - t1) / 1000000.0;
        LOGGER.info("Time to parse project: " + runtimeInMs);
    }

    private List<Path> getFilePathsInProject() {
        List<Path> filePaths = new ArrayList<>();

        try {
            URI uri = new URI(rootUri);
            filePaths = Files
                    .find(
                            Paths.get(uri),
                            Integer.MAX_VALUE,
                            (filePath, fileAttr) -> (fileAttr.isRegularFile() ||
                                    fileAttr.isDirectory()) &&
                                    com.google.common.io.Files
                                            .getFileExtension(filePath.toString())
                                            .equals("ccdetect"))
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
                        Charset.forName("UTF-8"))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            return content;
        } catch (IOException ex) {
            ex.printStackTrace(); // handle an exception here
        }

        return null;
    }

    @Override
    public void updateDocument(String uri, TreesitterDocumentModel updatedDocument) {

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
    public Iterator<TreesitterDocumentModel> iterator() {
        return documents.values().iterator();
    }

    @Override
    public void updateDocument(String uri, Range range, String updatedContent) {

        double t1 = System.nanoTime();
        TreesitterDocumentModel document = documents.get(uri);
        document.updateDocument(range, updatedContent);
        double t2 = System.nanoTime();
        double runtimeInMs = (t2 - t1) / 1000000.0;
        LOGGER.info("Time to incremental reparse: " + runtimeInMs);
    }

    @Override
    public boolean containsDocument(String uri) {
        return documents.containsKey(uri);
    }

}
