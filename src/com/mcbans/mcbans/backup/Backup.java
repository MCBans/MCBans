package com.mcbans.mcbans.backup;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class Backup {
	private ArrayList<String> players = new ArrayList<String>();
	private boolean debug = false;
	private String apiKey = "";
	public Backup(boolean Debug, String ApiKey){
		debug = Debug;
		apiKey=ApiKey;
		String strLine="";
		File backupFile = new File("plugins/mcbans/backup.txt");
		if(backupFile.exists()){
			try {
				FileInputStream backupRead = new FileInputStream("plugins/mcbans/backup.txt");
				BufferedReader i;
				i = new BufferedReader(new InputStreamReader(backupRead,"UTF8"));
			    String line = null;
			    while (( line = i.readLine()) != null){
			    	strLine += line;
			    }
			    i.close();
			} catch (UnsupportedEncodingException e) {
				if(debug){
					e.printStackTrace();
				}
			} catch (IOException e) {
				if(debug){
					e.printStackTrace();
				}
			}
	    }
		for(String player : strLine.split(",") ){
			players.add(player);
		}
	}
	public void fetch(){
		String result = "";
		try {
			URL url;
			url = new URL("http://72.10.39.172/v2/"+this.apiKey);
    	    URLConnection conn = url.openConnection();
    	    conn.setConnectTimeout(4000);
    	    conn.setReadTimeout(4000);
    	    conn.setDoOutput(true);
    	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    	    wr.write("exec=backup");
    	    wr.flush();
    	    StringBuilder buf = new StringBuilder();
    	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    	    String line;
    	    while ((line = rd.readLine()) != null) {
    	    	buf.append(line);
    	    }
    	    result = buf.toString();
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
		for(String player : result.split(",") ){
			players.add(player);
		}
		try {
			File deleteFile = new File("plugins/mcbans/backup.txt");
			deleteFile.delete();
			Writer writer;
			writer = new OutputStreamWriter(new FileOutputStream("plugins/mcbans/backup.txt"), "UTF-8");
			BufferedWriter fout = new BufferedWriter(writer);
			fout.write(result);
			fout.close();
			writer.close();
		} catch (UnsupportedEncodingException e) {
			if(debug){
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			if(debug){
				e.printStackTrace();
			}
		} catch (IOException e) {
			if(debug){
				e.printStackTrace();
			}
		}
	}
	public boolean add(String playerName){
		if(players.contains(playerName)){
			return false;
		}
		String fileData = playerName;
		for(String player : players ){
			fileData+=","+player;
		}
		players.add(playerName);
		try {
			File deleteFile = new File("plugins/mcbans/backup.txt");
			deleteFile.delete();
			Writer writer;
			writer = new OutputStreamWriter(new FileOutputStream("plugins/mcbans/backup.txt"), "UTF-8");
			BufferedWriter fout = new BufferedWriter(writer);
			fout.write(fileData);
			fout.close();
			writer.close();
			return true;
		} catch (UnsupportedEncodingException e) {
			if(debug){
				e.printStackTrace();
			}
			return false;
		} catch (FileNotFoundException e) {
			if(debug){
				e.printStackTrace();
			}
			return false;
		} catch (IOException e) {
			if(debug){
				e.printStackTrace();
			}
			return false;
		}
	}
	public boolean remove(String playerName){
		if(players.contains(playerName)){
			players.remove(playerName);
			this.save();
			return true;
		}else{
			return false;
		}
	}
	public void save(){
		String fileData = "";
		for(String player : players ){
			if(fileData.equalsIgnoreCase("")){
				fileData=player;
			}else{
				fileData+=","+player;
			}
		}
		try {
			File deleteFile = new File("plugins/mcbans/backup.txt");
			deleteFile.delete();
			Writer writer;
			writer = new OutputStreamWriter(new FileOutputStream("plugins/mcbans/backup.txt"), "UTF-8");
			BufferedWriter fout = new BufferedWriter(writer);
			fout.write(fileData);
			fout.close();
			writer.close();
		} catch (UnsupportedEncodingException e) {
			if(debug){
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			if(debug){
				e.printStackTrace();
			}
		} catch (IOException e) {
			if(debug){
				e.printStackTrace();
			}
		}
	}
	public boolean isBanned( String playerName ){
		return players.contains(playerName);
	}
}