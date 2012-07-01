package com.mcbans.firestar.mcbans.callBacks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class BanSync implements Runnable {
	private final BukkitInterface MCBans;
	public long last_req=0;
	private long timeRecieved = 0;
	public BanSync(BukkitInterface p){
		MCBans = p;
		this.load();
	}
	@Override
	public void run(){
		int syncInterval = ((60*1000)*MCBans.Settings.getInteger("syncInterval"));
		if(syncInterval<((60*1000)*5)){
			syncInterval=((60*1000)*5);
		}
		while(true){
			while(MCBans.notSelectedServer){
				//waiting for server select
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			this.mainRequest();
			MCBans.lastSync = System.currentTimeMillis()/1000;
			try {
				Thread.sleep(syncInterval);
			} catch (InterruptedException e) {
			}
		}
	}
    public void goRequest() {
        this.mainRequest();
    }
	private void mainRequest(){
		if(MCBans.lastID==0){
			this.initialSync();
			this.save();
		}else{
			this.startSync();
			this.save();
		}
	}
	public void initialSync(){
		if(MCBans.syncRunning==true){
			return;
		}
		MCBans.syncRunning = true;
		boolean goNext = true;
		while(goNext){
			JsonHandler webHandle = new JsonHandler( MCBans );
			HashMap<String, String> url_items = new HashMap<String, String>();
			url_items.put( "latestSync", String.valueOf(MCBans.lastID) );
			url_items.put( "timeRecieved", String.valueOf(timeRecieved) );
			url_items.put( "exec", "banSyncInitial" );
			JSONObject response = webHandle.hdl_jobj(url_items);
			try {
				if(response.has("banned")){
					if (response.getJSONArray("banned").length() > 0) {
				    	for (int v = 0; v < response.getJSONArray("banned").length(); v++) {
				    		String[] plyer = response.getJSONArray("banned").getString(v).split(";");
				    		OfflinePlayer d = MCBans.getServer().getOfflinePlayer(plyer[0]);
							if(d.isBanned()){
								if(plyer[1].equals("u")){
									d.setBanned(false);
								}
							}else{
								if(plyer[1].equals("b")){
									d.setBanned(true);
								}
							}
				    	}
					}
				}
				if(MCBans.lastID == 0){
					if(response.has("timerecieved")){
						timeRecieved = response.getLong("timerecieved");
					}
				}
				if(response.has("lastid")){
					MCBans.lastID = response.getLong("lastid");
				}
				if(response.has("more")){
					goNext = true;
				}else{
					goNext = false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		MCBans.syncRunning = false;
	}
	public void startSync(){
		if(MCBans.syncRunning==true){
			return;
		}
		MCBans.syncRunning = true;
		boolean goNext = true;
		while(goNext){
			JsonHandler webHandle = new JsonHandler( MCBans );
			HashMap<String, String> url_items = new HashMap<String, String>();
			url_items.put( "latestSync", String.valueOf(MCBans.lastID) );
			url_items.put( "exec", "banSync" );
			JSONObject response = webHandle.hdl_jobj(url_items);
			try {
				if(response.has("banned")){
					if (response.getJSONArray("banned").length() > 0) {
				    	for (int v = 0; v < response.getJSONArray("banned").length(); v++) {
				    		String[] plyer = response.getJSONArray("banned").getString(v).split(";");
				    		OfflinePlayer d = MCBans.getServer().getOfflinePlayer(plyer[0]);
				    		if(d.isBanned()){
								if(plyer[1].equals("u")){
									d.setBanned(false);
								}
							}else{
								if(plyer[1].equals("b")){
									d.setBanned(true);
								}
							}
				    	}
					}
				}
				if(response.has("lastid")){
					long h = response.getLong("lastid");
					if(h != 0){
						MCBans.lastID = h;
					}
				}
				if(response.has("more")){
					goNext = true;
				}else{
					goNext = false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		MCBans.syncRunning = false;
	}
	public void save(){
		try {
			Writer writer = new OutputStreamWriter(
	                new FileOutputStream("plugins/mcbans/sync.last"), "UTF-8");
			BufferedWriter fout = new BufferedWriter(writer);
			fout.write(String.valueOf(MCBans.lastID));
			fout.close();
			writer.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	public void load(){
		File f = new File("plugins/mcbans/sync.last");
		if(f.exists()!=true){
			MCBans.lastID=0;
			return;
		}
		String strLine="";
		try {
		    BufferedReader i = new BufferedReader(new InputStreamReader
                    (new FileInputStream("plugins/mcbans/sync.last"),"UTF8"));
		    String line = null;
		    while (( line = i.readLine()) != null){
		    	strLine += line;
		    }
		    i.close();
		    MCBans.lastID=Integer.valueOf(strLine);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
}