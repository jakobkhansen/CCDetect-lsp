package CCDetect.lsp.files.fileiterators;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FiletypeIterator implements ProjectFileIterator {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);
    String rootUri;
    String filetype;

    public FiletypeIterator(String rootUri, String filetype) {
        this.rootUri = rootUri;
        this.filetype = filetype;
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
                                            .equals(filetype))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }

        return filePaths;
    }

    @Override
    public Iterator<Path> iterator() {
        return getFilePathsInProject().iterator();
    }
}
