package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class ManualSync implements Runnable {
	private final BukkitInterface MCBans;
	private String commandSend = "";
	public ManualSync(BukkitInterface p, String player){
		MCBans = p;
		commandSend = player;
	}
	@Override
	public void run() {
		if(MCBans.syncRunning==true){
			return;
		}
		while(MCBans.notSelectedServer){
			//waiting for server select
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		int fre = 0;
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
					fre += response.getJSONArray("banned").length();
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
		MCBans.broadcastPlayer(commandSend, ChatColor.GREEN + " Sync finished, "+fre+" actions!" );
	}
	
}