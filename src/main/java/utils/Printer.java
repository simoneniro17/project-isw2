package utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Printer {
    
    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    private Printer() {}
    
    /**
     * Print an error message
     * @param error the error message to print
     */
    public static void printError(String error) {
        logger.log(Level.SEVERE, error);
    }
    
    /**
     * Print an info message
     * @param message the info message to print
     */
    public static void printMessage(String message) {
        logger.log(Level.INFO, message);
    }
}
