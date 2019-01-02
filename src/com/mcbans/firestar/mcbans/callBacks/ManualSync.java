package com.mcbans.firestar.mcbans.callBacks;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.util.Util;
import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.Date;

public class ManualSync implements Runnable {
    private final MCBans plugin;
    private final String commandSend;
    
    public ManualSync(final MCBans plugin, final String sender){
        this.plugin = plugin;
        this.commandSend = sender;
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public void run() {
        while(plugin.apiServer == null){
            //waiting for server select
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignore) {}
        }
        
        if(plugin.syncRunning){
            Util.message(commandSend, ChatColor.GREEN + "Sync is already running." );
            return;
        }
        plugin.syncRunning = true;
        int changes = 0;
        try{
            boolean goNext = true;
            while(goNext){
	            if(String.valueOf(plugin.lastType).equals("") || String.valueOf(plugin.lastType) == null || String.valueOf(plugin.lastID).equals("")){
        			plugin.lastType = "bans";
        			plugin.lastID = 0;
        			goNext =false;
        		}else{
		            JSONObject response = ManualReSync.getJson(plugin);
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
	                            	changes++;
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
	                        	plugin.debug("Recieved "+plugin.lastType+" from: "+plugin.lastID+" to: "+response.getLong("lastid"));
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
            }
        } finally {
            plugin.syncRunning = false;
        }
        plugin.lastSync = System.currentTimeMillis() / 1000;
        Util.message(commandSend, ChatColor.GREEN + "Sync is complete with " + changes + " actions." );
        save();
    }

	private void save(){
		ManualReSync.syncInfo(plugin);
    }
}