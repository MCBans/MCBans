package com.mcbans.firestar.mcbans.log;
/**
 * Utilities log (FROM: http://www.rgagnon.com)
 */
import java.io.*;
import java.text.*;
import java.util.*;

import com.mcbans.firestar.mcbans.bukkitInterface;

public class ActionLog {

    private static String logFile = "";
    private bukkitInterface MCBans = null;
    private final static DateFormat df = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss ");

    public ActionLog( bukkitInterface p, String logfile ) {
    	logFile = logfile;
    	MCBans = p; 
    }
    
    
    public void write(String msg) {
    	if(MCBans.Settings.getBoolean("logEnable")){
    		write(logFile, msg);
    	}else{
    		System.out.println("MCBans: " + msg);
    	}
    }
    
    public void write(Exception e) {
    	if(MCBans.Settings.getBoolean("logEnable")){
    		write(logFile, stack2string(e));
    	}else{
    		System.out.println("MCBans: " + stack2string(e));
    	}
    }

    public static void write(String file, String msg) {
        try {
            Date now = new Date();
            String currentTime = ActionLog.df.format(now); 
            FileWriter aWriter = new FileWriter(file, true);
            aWriter.write(currentTime + " " + msg 
                    + System.getProperty("line.separator"));
            //System.out.println(currentTime + " " + msg);
            aWriter.flush();
            aWriter.close();
        }
        catch (Exception e) {
            System.out.println(stack2string(e));
        }
    }
    
    private static String stack2string(Exception e) {
        try {
            //StringWriter sw = new StringWriter();
            //PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "------\r\n" + sw.toString() + "------\r\n";
        }
        catch(Exception e2) {
            return "bad stack2string";
        }
    }
}