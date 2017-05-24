package se.gorymoon.vctweaker.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gorymoon.vctweaker.VCTweaker;


public class Log {
    private static final Logger LOGGER = LogManager.getLogger(VCTweaker.MODID);

    private Log() {
    }

    public static void trace(String message, Object... params) {
        LOGGER.log(Level.TRACE, message, params);
    }

    public static void debug(String message, Object... params) {
        LOGGER.log(Level.DEBUG, message, params);
    }

    public static void info(String message, Object... params) {
        LOGGER.log(Level.INFO, message, params);
    }

    public static void warning(String message, Object... params) {
        LOGGER.log(Level.WARN, message, params);
    }

    public static void error(String message, Object... params) {
        LOGGER.log(Level.ERROR, message, params);
    }

}
