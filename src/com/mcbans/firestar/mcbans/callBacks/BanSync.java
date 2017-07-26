package com.mcbans.firestar.mcbans.callBacks;

import static com.mcbans.firestar.mcbans.I18n._;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.BanList;
import org.bukkit.ChatColor;

import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.bukkitListeners.PlayerListener;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class BanSync implements Runnable {
    private final MCBans plugin;

    public BanSync(MCBans plugin){
        this.plugin = plugin;
    }

    @Override
    public void run(){
        while(true){
            int syncInterval = ((60 * 1000) * plugin.getConfigs().getSyncInterval());
            if(syncInterval < ((60 * 1000) * 60)){
                syncInterval = ((60 * 1000) * 60);
            }
            while(plugin.apiServer == null){
                //waiting for server select
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }

            // check isEnable auto syncing feature
            if (plugin.getConfigs().isEnableAutoSync()){
            	this.startSync();
                plugin.lastSync = System.currentTimeMillis() / 1000;
            }

            try {
                Thread.sleep(syncInterval);
            } catch (InterruptedException e) {}
        }
    }

    public void goRequest() {
    	this.startSync();
    }

	public void startSync(){
        if(plugin.syncRunning){
            return;
        }
        plugin.syncRunning = true;
        
        try{
            boolean goNext = true;
        	while(goNext){
        		if(String.valueOf(plugin.lastType).equals("") || String.valueOf(plugin.lastType)==null || String.valueOf(plugin.lastID)==null || String.valueOf(plugin.lastID).equals("")){
        			plugin.lastType = "bans";
        			plugin.lastID = 0;
        			goNext =false;
        			System.out.println(ChatColor.RED+"MCBans: Error resetting sync. Please report to staff!");
        		}else{
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
	                            	OfflinePlayer d = plugin.getServer().getPlayer(plyer.getString("name"));
	                    	    	if (d != null){
	                    		    	if(d.isBanned()){
	                    		            if(plyer.getString("do").equals("unban")){
	                    		            	if (plugin.getServer().getBanList(BanList.Type.NAME).isBanned(d.getName())){
	                    		            		plugin.getServer().getBanList(BanList.Type.NAME).pardon(d.getName());
	                    		                }
	                    		            }
	                    		        }else{
	                    		            if(plyer.getString("do").equals("ban")){
	                    		                if (!plugin.getServer().getBanList(BanList.Type.NAME).isBanned(d.getName())){
	                    		                	plugin.getServer().getBanList(BanList.Type.NAME).addBan(d.getName(), "", new Date(), "sync");
	                    		                }
	                    		                PlayerListener.cache.invalidate(plyer.getString("name"));
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
	                    		plugin.debug("Bans have been retrieved!");
	                        }else if(plugin.lastID==response.getLong("lastid") && plugin.lastType.equalsIgnoreCase("sync")){
	                        	plugin.debug("Sync has completed!");
	                        	goNext = false;
	                        }else{
	                        	plugin.debug("Received "+plugin.lastType+" from: "+plugin.lastID+" to: "+response.getLong("lastid"));
	                        	plugin.lastID=response.getLong("lastid");
	                        }
	            		}
	                    save();
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
            }
        } finally {
            plugin.syncRunning = false;
        }
        plugin.lastSync = System.currentTimeMillis() / 1000;
        save();
    }
    public void save(){
    	plugin.lastSyncs.setProperty("lastId", String.valueOf(plugin.lastID));
    	plugin.lastSyncs.setProperty("lastType", String.valueOf(plugin.lastType));
    	try {
			plugin.lastSyncs.store(new FileOutputStream(plugin.syncIni), "Syncing ban information!");
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
