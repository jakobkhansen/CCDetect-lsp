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

/**
 * Treesitter parser singleton
 */
public class Treesitter {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    private static final String LIB = "libparser.so";

    private static Parser parser;

    static {
        LOGGER.info("Building parser");
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

            parser = new Parser();
            parser.setLanguage(Languages.java());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Parser getParser() {
        return parser;
    }
}
