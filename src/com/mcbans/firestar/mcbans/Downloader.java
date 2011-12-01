package com.mcbans.firestar.mcbans;

import com.mcbans.firestar.mcbans.log.LogLevels;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {
	public Downloader (BukkitInterface MCBans) {
		MCBans.log(LogLevels.INFO, "Waiting for download request.");
		File mcbansFolder = new File("plugins/mcbans");
		if (!mcbansFolder.exists()) {
			mcbansFolder.mkdir();
			MCBans.log(LogLevels.INFO, "Created plugin directory..");
		}
		File languageFolder = new File("plugins/mcbans/language");
		if (!languageFolder.exists()) {
			languageFolder.mkdir();
			MCBans.log(LogLevels.INFO, "Created language directory..");
		}
	}
	
	public void Download(String address, String localFileName) {
	    OutputStream out = null;
	    URLConnection conn = null;
	    InputStream in = null;
	    try {
	        // Get the URL
	        URL url = new URL(address);
	        // Open an output stream to the destination file on our local filesystem
	        out = new BufferedOutputStream(new FileOutputStream(localFileName));
	        conn = url.openConnection();
	        in = conn.getInputStream();
	 
	        // Get the data
	        byte[] buffer = new byte[1024];
	        int numRead;
	        while ((numRead = in.read(buffer)) != -1) {
	            out.write(buffer, 0, numRead);
	        }           
	        // Done! Just clean up and get out
	    } catch (Exception exception) {
	        exception.printStackTrace();
	    } finally {
	        try {
	            if (in != null) {
	                in.close();
	            }
	            if (out != null) {
	                out.close();
	            }
	            System.out.print("MCBans: Download completed.");
	        } catch (IOException ioe) {
	            System.out.print("MCBans: Download unsuccessful.");
	        }
	    }
	}
}
