package org.theorangealliance.datasync.logging;

import java.io.IOException;
import java.util.logging.*;

/**
 * Created by Kyle Flynn on 12/3/2017.
 */
public class TOALogger {

    private static Logger logger;

    public static void init() throws IOException {
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        Formatter logFormatter = new LogFileFormatter();

        FileHandler logFile = new FileHandler("log.log", true);
        ConsoleHandler console = new ConsoleHandler();

        logFile.setFormatter(logFormatter);
        console.setFormatter(logFormatter);
        logger.addHandler(logFile);
        logger.addHandler(console);
    }

    public static void setLevel(Level level) {
        logger.setLevel(level);
    }

    public static void log(Level level, String message) {
        logger.log(level, message);
    }

}
