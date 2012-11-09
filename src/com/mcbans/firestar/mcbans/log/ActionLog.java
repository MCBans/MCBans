package com.mcbans.firestar.mcbans.log;

/**
 * Utilities log (FROM: http://www.rgagnon.com)
 */

import java.io.FileWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mcbans.firestar.mcbans.MCBans;

public class ActionLog {
    private static String logFile = "";
    private static MCBans plugin = null;
    private final static DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss ");

    public ActionLog(MCBans p, String logfile) {
        logFile = logfile;
        plugin = p;
    }

    public void write(String msg) {
        if (plugin.settings.getBoolean("logEnable")) {
            write(logFile, msg);
        }
    }

    public void write(Exception e) {
        if (plugin.settings.getBoolean("logEnable")) {
            write(logFile, stack2string(e));
        }
        plugin.log(stack2string(e));
    }

    public static void write(String file, String msg) {
        try {
            Date now = new Date();
            String currentTime = ActionLog.df.format(now);
            FileWriter aWriter = new FileWriter(file, true);
            aWriter.write(currentTime + " " + msg + System.getProperty("line.separator"));
            // System.out.println(currentTime + " " + msg);
            aWriter.flush();
            aWriter.close();
        } catch (Exception e) {
            plugin.log(LogLevels.WARNING, stack2string(e));
        }
    }

    private static String stack2string(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            // PrintWriter pw = new PrintWriter(sw);
            // e.printStackTrace(pw);
            return "------\r\n" + sw.toString() + "------\r\n";
        } catch (Exception e2) {
            return "bad stack2string";
        }
    }
}