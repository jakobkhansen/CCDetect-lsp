package CCDetect.lsp.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * CCLogger
 */
public class CCFileStateLogger {

    private static FileHandler fileTxt;
    private static SimpleFormatter formatterTxt;

    public static void setup() throws IOException {

        Logger logger = Logger.getLogger("CCFileStateLogger");


        logger.setLevel(Level.INFO);

        File dir = new File("logs");
        if (!dir.exists()) dir.mkdirs();
        fileTxt = new FileHandler("logs/file.txt");

        // create a TXT formatter
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
    }
    
}
