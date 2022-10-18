package CCDetect.lsp.utils;

import java.util.logging.Logger;

public class Timer {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    double start;
    double stop;

    public void start() {
        start = System.nanoTime();
    }

    public void stop() {
        stop = System.nanoTime();
    }

    public double getTotal() {
        return (stop - start) / 1000000.0;

    }

    public void log(String message) {
        LOGGER.info(message + ": " + getTotal());
    }
}
