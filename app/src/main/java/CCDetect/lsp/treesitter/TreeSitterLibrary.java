package CCDetect.lsp.treesitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import CCDetect.lsp.server.Configuration;
import ai.serenade.treesitter.Node;
import ai.serenade.treesitter.Parser;
import ai.serenade.treesitter.TSQuery;
import ai.serenade.treesitter.TSQueryCursor;

/**
 * Treesitter parser singleton and interface for treesitter operations
 */
public class TreeSitterLibrary {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    private static final String LIB = "libparser.so";

    private static Parser parser;

    static {
        LOGGER.info("Loading treesitter");
        try {
            InputStream in = TreeSitterLibrary.class.getResourceAsStream("/libparser.so");

            File fileOut = new File(
                    System.getProperty("java.io.tmpdir") + "/" + LIB);

            OutputStream outStream = new FileOutputStream(fileOut);

            Files.copy(
                    in,
                    fileOut.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            System.load(fileOut.getAbsolutePath());
            outStream.close();

            LOGGER.info("Treesitter loaded");
        } catch (Exception e) {
            LOGGER.info(e.toString());
        } finally {

        }
    }

    public static void ensureLoaded() {
    }

    private static void createParser() {

        parser = new Parser();
        parser.setLanguage(getLanguage());
    }

    public static Parser getParser() {
        if (parser == null) {
            createParser();
        }
        return parser;
    }

    public static TSQueryCursor queryPattern(Node node, String pattern) {
        TSQuery query = new TSQuery(getLanguage(), pattern);
        TSQueryCursor cursor = new TSQueryCursor();

        cursor.execQuery(query, node);

        return cursor;
    }

    private static long getLanguage() {
        Configuration config = Configuration.getInstance();
        String language = config.getLanguage();

        return TreeSitterLanguageResolver.resolve(language);
    }
}
