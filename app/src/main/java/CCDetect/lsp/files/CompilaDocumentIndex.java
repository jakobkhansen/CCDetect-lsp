package CCDetect.lsp.files;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CCDetect.lsp.CodeClone;

/**
 * DocumentIndex
 */
public class CompilaDocumentIndex implements DocumentIndex {
    Map<String, DocumentModel> documents = Collections.synchronizedMap(new HashMap<>());
    String rootUri;

    public CompilaDocumentIndex(String rootUri) {
        this.rootUri = rootUri;
    }

    @Override
    public void indexProject() {
        // TODO Auto-generated method stub
    }

    private List<String> getFilePathsInProject() {
        List<String> filePaths = new ArrayList<>();
        
        try {
            URI uri = new URI(rootUri);
        } catch (URISyntaxException e) {

            e.printStackTrace();
        }


        return filePaths;
    }

    @Override
    public DocumentModel getDocument(String uri) {
        return documents.get(uri);
    }

    @Override
    public void updateClones(List<CodeClone> clones) {
        // TODO Auto-generated method stub
        
    }

    
    
}
