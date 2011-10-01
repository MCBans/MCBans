package com.mcbans.firestar.mcbans;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class Core {
	
	public String apikey;
	public String lang;
	
	public Core () {
		InputStream in;
		try {
			in = Core.class.getClassLoader().getResourceAsStream("core.yml");
		} catch (NullPointerException ex) {
			return;
		}
		Yaml yaml = new Yaml();
		Map map = (Map)yaml.load(in);
		this.apikey = (String) map.get("apikey");
		this.lang = (String) map.get("lang");
	}
	
	public void download(String address, String localFileName) {
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
	        } catch (IOException ioe) {
	            // Shouldn't happen, maybe add some logging here if you are not 
	            // fooling around ;)
	        }
	    }
	}
}
