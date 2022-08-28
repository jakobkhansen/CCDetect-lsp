package CCDetect.lsp.treesitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import ai.serenade.treesitter.Languages;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.Tree;

/**
 * Treesitter
 */
public class Treesitter {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    private static final String LIB = "libparser.so";

    static {
        try {
            InputStream in = Treesitter.class.getResourceAsStream("/libparser.so");

            File fileOut = new File(
                    System.getProperty("java.io.tmpdir") + "/" + LIB);

            OutputStream outStream = new FileOutputStream(fileOut);

            Files.copy(
                    in,
                    fileOut.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            System.load(fileOut.getAbsolutePath());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hello() {
        try {
            Parser parser = new Parser();
            parser.setLanguage(Languages.java());

            Tree tree = parser.parseString("class Hello() {}");

            LOGGER.info(tree.getRootNode().getNodeString());

            parser.close();
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
    }
}
