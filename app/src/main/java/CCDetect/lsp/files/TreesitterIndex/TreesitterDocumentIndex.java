package CCDetect.lsp.files.TreesitterIndex;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.lsp4j.Range;

import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.files.fileiterators.GitProjectIterator;
import CCDetect.lsp.files.fileiterators.ProjectFileIterator;
import CCDetect.lsp.server.Configuration;
import CCDetect.lsp.utils.Timer;

/**
 * DocumentIndex
 */
public class TreesitterDocumentIndex implements DocumentIndex<TreesitterDocumentModel> {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    Map<String, TreesitterDocumentModel> documents = Collections.synchronizedMap(
            new HashMap<>());
    String rootUri;
    ProjectFileIterator projectFileIterator;

    public TreesitterDocumentIndex(String rootUri, ProjectFileIterator projectFileIterator) {
        this.rootUri = rootUri;
        this.projectFileIterator = projectFileIterator;
    }

    @Override
    public void indexProject() {
        List<Path> filePaths = getFilePathsInProject();
        for (Path p : filePaths) {
            TreesitterDocumentModel model = new TreesitterDocumentModel(p, null);
            updateDocument(p.toUri().toString(), model);
        }
    }

    private List<Path> getFilePathsInProject() {
        return StreamSupport.stream(projectFileIterator.spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public void updateDocument(String uri, TreesitterDocumentModel updatedDocument) {
        documents.put(uri, updatedDocument);
    }

    @Override
    public TreesitterDocumentModel getDocument(String uri) {
        return documents.get(uri);
    }

    @Override
    public Iterator<TreesitterDocumentModel> iterator() {
        return documents.values().iterator();
    }

    @Override
    public void updateDocument(String uri, Range range, String updatedContent) {
        Timer timer = new Timer();
        timer.start();
        TreesitterDocumentModel document = documents.get(uri);
        document.updateDocument(range, updatedContent);
        document.setChanged(true);
        timer.stop();

        timer.log("Time to incremental reparse");
    }

    @Override
    public boolean containsDocument(String uri) {
        return documents.containsKey(uri);
    }

    @Override
    public int size() {
        return documents.size();
    }

}
