package CCDetect.lsp.files.fileiterators;

import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * GitProjectIterator
 */
public class GitProjectIterator implements ProjectFileIterator {

    String rootUri;
    String filetype;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public GitProjectIterator(String rootUri, String filetype) {
        this.rootUri = rootUri;
        this.filetype = filetype;
    }

    private List<Path> getFilePathsInProject() {
        List<Path> filePaths = new ArrayList<>();
        String command = String.format("git --git-dir=%s/.git ls-files", rootUri);
        LOGGER.info("command " + command);
        try {
            Process p = Runtime.getRuntime().exec(command);
            Scanner scan = new Scanner(new InputStreamReader(p.getInputStream()));
            while (scan.hasNextLine()) {
                String pathString = scan.nextLine();

                // Filter out non filetype if filetype is given
                if (filetype != null) {
                    if (!com.google.common.io.Files
                            .getFileExtension(pathString)
                            .equals(filetype)) {
                        continue;
                    }
                }
                filePaths.add(Paths.get(rootUri, pathString));
            }
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
        LOGGER.info(filePaths.size() + "");
        return filePaths;
    }

    @Override
    public Iterator<Path> iterator() {
        // TODO Auto-generated method stub
        return getFilePathsInProject().iterator();
    }

}
