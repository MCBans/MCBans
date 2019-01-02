package com.mcbans.firestar.mcbans.callBacks;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;
import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class ManualReSync implements Runnable{
    private final MCBans plugin;
    private final String commandSend;

	public ManualReSync(final MCBans plugin, final String sender){
        this.plugin = plugin;
        this.commandSend = sender;
    }

	static JSONObject getJson(MCBans plugin){
		return getJsonObject(plugin);
	}

	static JSONObject getJsonObject(MCBans plugin){
		JsonHandler webHandle = new JsonHandler(plugin);
		HashMap<String, String> url_items = new HashMap<>();
		url_items.put("lastId", String.valueOf(plugin.lastID));
		url_items.put("lastType", String.valueOf(plugin.lastType));
		url_items.put("exec", "banSync");
		return webHandle.hdl_jobj(url_items);
	}

	static void syncInfo(MCBans plugin){
		plugin.lastSyncs.setProperty("lastId", String.valueOf(plugin.lastID));
		plugin.lastSyncs.setProperty("lastType", String.valueOf(plugin.lastType));
		try{
			plugin.lastSyncs.store(new FileOutputStream(plugin.syncIni), "Syncing ban information.");
		}catch(IOException e){
			if(plugin.getConfigs().isDebug()){
				e.printStackTrace();
			}
		}
	}

    @SuppressWarnings("deprecation")
	@Override
    public void run() {
        if(plugin.syncRunning){
            Util.message(commandSend, ChatColor.GREEN + "Sync is already running." );
            return;
        }
        plugin.syncRunning = true;
        try{
            boolean goNext = true;
            plugin.lastID = 0;
            plugin.lastType = "bans";
            while(goNext){

	            JSONObject response = getJson(plugin);
	            try{
                    if(response.has("actions")){
                        if (response.getJSONArray("actions").length() > 0) {
                            for (int v = 0; v < response.getJSONArray("actions").length(); v++) {
                            	JSONObject plyer = response.getJSONArray("actions").getJSONObject(v);
                            	//plugin.act( plyer.getString("do"), plyer.getString("uuid"));
                            	OfflinePlayer d = plugin.getServer().getOfflinePlayer(plyer.getString("name"));
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
	            }catch(JSONException | NullPointerException e){
                    if(plugin.getConfigs().isDebug()){
                        e.printStackTrace();
                    }
                }
	            try{
                	Thread.sleep(5000);
                } catch (InterruptedException ignore) {}
            }
        } finally {
            plugin.syncRunning = false;
        }
        plugin.lastSync = System.currentTimeMillis() / 1000;
        Util.message(commandSend, ChatColor.GREEN + "Sync is complete.");
        save();
    }

	private void save(){
		syncInfo(plugin);
	}
}
