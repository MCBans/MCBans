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
			MCBans.broadcastPlayer( PlayerAdmin, "MCBans is currently in " + ChatColor.DARK_RED + "OFFLINE" + ChatColor.WHITE + " mode." );
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
			MCBans.broadcastPlayer( PlayerAdmin, "Player " + ChatColor.AQUA + PlayerName + ChatColor.WHITE + " has " + ChatColor.DARK_RED + result.getString("total") + " ban(s) " + ChatColor.WHITE + " and " + ChatColor.LIGHT_PURPLE + result.getString("reputation") + " REP" + ChatColor.WHITE + "." );
	        if (result.getJSONArray("global").length() > 0) {
	        	MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + "Global bans");
	        	for (int v = 0; v < result.getJSONArray("global").length(); v++) {
	        		MCBans.broadcastPlayer( PlayerAdmin, result.getJSONArray("global").getString(v) );
	        	}
	        }
	        if (result.getJSONArray("local").length() > 0) {
	        	MCBans.broadcastPlayer( PlayerAdmin, ChatColor.YELLOW + "Local bans");
	        	for (int v = 0; v < result.getJSONArray("local").length(); v++) {
	        		MCBans.broadcastPlayer( PlayerAdmin, result.getJSONArray("local").getString(v) );
	        	}
	        }
        } catch (JSONException e) {
        } catch (NullPointerException e) {
		}
	}
}