package org.theorangealliance.datasync.logging;

import java.io.File;
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

        File logDir = new File(System.getProperty("user.home") + File.separator + "log");

        if (!logDir.exists()) {
            System.out.println(logDir.getAbsolutePath());
            logDir.mkdir();
        }

        File loggingFile = new File(logDir.getAbsolutePath() + File.separator + "log.log");

        if (!loggingFile.exists()) {
            loggingFile.createNewFile();
        }

        File lockFile = new File(logDir.getAbsolutePath() + File.separator + "log.log.lck");

        if (!lockFile.exists()) {
            lockFile.createNewFile();
        }

        FileHandler logFile = new FileHandler(loggingFile.getAbsolutePath());
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
