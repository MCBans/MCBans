package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.json.JSONException;
import org.json.JSONObject;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;

public class lookup extends Thread {
	private bukkitInterface MCBans;
	private String PlayerName;
	private String PlayerAdmin;
	public lookup( bukkitInterface p, String playerName, String playerAdmin ){
		MCBans = p;
		PlayerName = playerName;
		PlayerAdmin = playerAdmin;
	}
	@Override
	public void run(){
		if(MCBans.getMode()){
			MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + "Offline Mode!" );
			return;
		}
		MCBans.log.write( PlayerAdmin + " has looked up the " + PlayerName + "!" );
		HashMap<String, String> url_items = new HashMap<String, String>();
		jsonHandler webHandle = new jsonHandler( MCBans );
        url_items.put("player", PlayerName);
        url_items.put("admin", PlayerAdmin);
        url_items.put("exec", "playerLookup");
        JSONObject result = webHandle.hdl_jobj(url_items);
        try {
			MCBans.broadcastPlayer( PlayerAdmin, ChatColor.YELLOW + PlayerName + " has " + result.getString("total") + " ban(s) .:. " + result.getString("reputation") + "/10 Reputation" );
	        for (int v = 0; v < result.getJSONArray("local").length(); v++) {
	        	MCBans.broadcastPlayer( PlayerAdmin, "[Local] " + ChatColor.AQUA + result.getJSONArray("local").getString(v) );
	        }
	        for (int v = 0; v < result.getJSONArray("global").length(); v++) {
	        	MCBans.broadcastPlayer( PlayerAdmin, "[Global] " + ChatColor.DARK_RED + result.getJSONArray("global").getString(v) );
	        }
        } catch (JSONException e) {
		}
	}
}