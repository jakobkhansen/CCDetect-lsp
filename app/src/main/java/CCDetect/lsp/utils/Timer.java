package CCDetect.lsp.utils;

import java.util.logging.Logger;

public class Timer {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    double start;
    double stop;
    boolean stdout = false;

    public Timer() {
    }

    public Timer(boolean stdout) {
        this.stdout = stdout;
    }

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
        if (stdout) {
            System.out.println(message + ":" + getTotal());
            return;
        }
        LOGGER.info(message + ": " + getTotal());
    }
}
