package com.mcbans.firestar.mcbans.backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.mcbans.firestar.mcbans.bukkitInterface;

public class backupCheck extends Thread {
	private bukkitInterface MCBans;
	private String apiKey = null;
	private boolean debug = false;
	public backupCheck( bukkitInterface p ){
		MCBans = p;
		apiKey = MCBans.getApiKey();
		debug = MCBans.Settings.getBoolean("isDebug");
	}
	@Override
	public void run(){
		while(true){
			String result = "";
			try {
				URL url;
				url = new URL( "http://72.10.39.172/v2/" + this.apiKey );
	    	    URLConnection conn = url.openConnection();
	    	    conn.setConnectTimeout(5000);
	    	    conn.setReadTimeout(5000);
	    	    conn.setDoOutput(true);
	    	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    	    wr.write("exec=check");
	    	    wr.flush();
	    	    StringBuilder buf = new StringBuilder();
	    	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    	    String line;
	    	    while ((line = rd.readLine()) != null) {
	    	    	buf.append(line);
	    	    }
	    	    result = buf.toString();
	    	    if(debug){
	    	    	MCBans.log.write(result);
	    	    }
	    	    wr.close();
	    	    rd.close();
			} catch (MalformedURLException e) {
				if(debug){
					e.printStackTrace();
				}
			} catch (IOException e) {
				if(debug){
					e.printStackTrace();
				}
			}
			if(!result.equalsIgnoreCase("up")){
				MCBans.setMode(true);
				MCBans.log.write("MCBans Master Server is offline!");
			}else{
				MCBans.setMode(false);
			}
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				if(debug){
					e.printStackTrace();
				}
			}
		}
	}
}