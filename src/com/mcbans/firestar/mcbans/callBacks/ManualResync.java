package com.mcbans.firestar.mcbans.callBacks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;

public class ManualResync implements Runnable {
    private final MCBans plugin;
    private final String commandSend;
    
    public ManualResync(final MCBans plugin, final String sender){
        this.plugin = plugin;
        this.commandSend = sender;
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void run() {
        if(plugin.syncRunning){
            Util.message(commandSend, ChatColor.GREEN + " Sync already in progress!" );
            return;
        }
        plugin.syncRunning = true;
        try{
            boolean goNext = true;
            plugin.lastID = 0;
            plugin.lastType = "bans";
            while(goNext){
                JsonHandler webHandle = new JsonHandler( plugin );
                HashMap<String, String> url_items = new HashMap<String, String>();
                url_items.put( "lastId", String.valueOf(plugin.lastID) );
                url_items.put( "lastType", String.valueOf(plugin.lastType) );
                url_items.put( "exec", "banSync" );
                JSONObject response = webHandle.hdl_jobj(url_items);
                try {
                    if(response.has("actions")){
                        if (response.getJSONArray("actions").length() > 0) {
                            for (int v = 0; v < response.getJSONArray("actions").length(); v++) {
                            	JSONObject plyer = response.getJSONArray("actions").getJSONObject(v);
                            	//plugin.act( plyer.getString("do"), plyer.getString("uuid"));
                            	OfflinePlayer d = plugin.getServer().getOfflinePlayer(plyer.getString("name"));
                    	    	if (d != null){
                    		    	if(d.isBanned()){
                    		            if(plyer.getString("do").equals("unban")){
                    		                d.setBanned(false);
                    		            }
                    		        }else{
                    		            if(plyer.getString("do").equals("ban")){
                    		                d.setBanned(true);
                    		            }
                    		        }
                    	    	}
                            }
                        }
                    }
                    if(response.has("lastid")){
                    	if(response.getLong("lastid") == 0 && plugin.lastType.equalsIgnoreCase("bans")){
                    		plugin.lastType = "sync";
                    		plugin.lastID = 0;
                    		plugin.debug("Bans retrieved");
                        }else if(plugin.lastID==response.getLong("lastid") && plugin.lastType.equalsIgnoreCase("sync")){
                        	plugin.debug("Sync Completed");
                        	goNext = false;
                        }else{
                        	plugin.debug("Recieved "+plugin.lastType+" from: "+plugin.lastID+" to: "+response.getLong("lastid"));
                        	plugin.lastID=response.getLong("lastid");
                        }
            		}
                } catch (JSONException e) {
                    if(plugin.getConfigs().isDebug()){
                        e.printStackTrace();
                    }
                } catch (NullPointerException e) {
                    if(plugin.getConfigs().isDebug()){
                        e.printStackTrace();
                    }
                }
                try {
                	Thread.sleep(5000);
                } catch (InterruptedException ignore) {}
            }
        } finally {
            plugin.syncRunning = false;
        }
        plugin.lastSync = System.currentTimeMillis() / 1000;
        Util.message(commandSend, ChatColor.GREEN + " Sync finished");
        save();
    }
    public void save(){
    	plugin.lastSyncs.setProperty("lastId", String.valueOf(plugin.lastID));
    	plugin.lastSyncs.setProperty("lastType", String.valueOf(plugin.lastType));
    	try {
			plugin.lastSyncs.store(new FileOutputStream(plugin.syncIni), "Syncing information. DO NOT TOUCH!");
		} catch (FileNotFoundException e) {
			if(plugin.getConfigs().isDebug()){
				e.printStackTrace();
			}
		} catch (IOException e) {
			if(plugin.getConfigs().isDebug()){
				e.printStackTrace();
			}
		}
    }
}
