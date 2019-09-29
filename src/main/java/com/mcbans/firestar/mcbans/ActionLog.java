package com.mcbans.firestar.mcbans;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionLog {
    private final Logger logger = Logger.getLogger("Minecraft");
    private final String logPrefix = "[MCBans] ";

    private final MCBans plugin;
    private static ActionLog instance;

    public ActionLog(final MCBans plugin){
        instance = this;
        this.plugin = plugin;
    }

    public void log(final Level level, final String message, final boolean logToFile){
        logger.log(level, logPrefix + message);
        if (logToFile && plugin.getConfigs() != null && plugin.getConfigs().isEnableLog()) {
            writeLog(message);
        }
    }

    public void log(final Level level, final String message){
        log (level, message, true);
    }

    public void fine(final String message){
        log(Level.FINE, message);
    }
    public void info(final String message){
        log(Level.INFO, message);
    }
    public void warning(final String message){
        log(Level.WARNING, message);
    }
    public void severe(final String message){
        log(Level.SEVERE, message);
    }

    /**
     * Write message to mcbans log file
     * @param message message to write
     */
    private void writeLog(final String message){
        try {
            appendLine(plugin.getConfigs().getLogFile(),
                    "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + message);
        } catch (IOException ex) {
            logger.warning(logPrefix + "Could not write log file! " + ex.getMessage());
        }
    }

    /**
     * Append line to file
     * @param file target file
     * @param line message to append line
     * @throws IOException IOException
     */
    private void appendLine(final String file, final String line) throws IOException  {
        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(new FileWriter(file, true));
            outputStream.println(line);
        } finally {
            if(outputStream != null) {
                outputStream.close();
            }
        }
    }

    public static ActionLog getInstance(){
        return instance;
    }
}
