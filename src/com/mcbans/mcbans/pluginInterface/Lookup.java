package com.mcbans.mcbans.pluginInterface;

import com.mcbans.mcbans.BukkitInterface;
import com.mcbans.mcbans.request.JsonHandler;
import org.bukkit.ChatColor;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.player.SpoutPlayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Lookup extends Thread {
	private BukkitInterface MCBans;
	private String PlayerName;
	private String PlayerAdmin;
	public Lookup(BukkitInterface p, String playerName, String playerAdmin){
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
		JsonHandler webHandle = new JsonHandler( MCBans );
        url_items.put("player", PlayerName);
        url_items.put("admin", PlayerAdmin);
        url_items.put("exec", "playerLookup");
        JSONObject result = webHandle.hdl_jobj(url_items);
        try {
			MCBans.broadcastPlayer( PlayerAdmin, "Player " + ChatColor.DARK_AQUA + PlayerName + ChatColor.WHITE + " has " + ChatColor.DARK_RED + result.getString("total") + " ban(s)" + ChatColor.WHITE + " and " + ChatColor.BLUE + result.getString("reputation") + " REP" + ChatColor.WHITE + "." );
	        if (result.getJSONArray("global").length() > 0) {
	        	MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + "Global bans");
	        	for (int v = 0; v < result.getJSONArray("global").length(); v++) {
	        		MCBans.broadcastPlayer( PlayerAdmin, result.getJSONArray("global").getString(v) );
	        	}
	        }
	        if (result.getJSONArray("local").length() > 0) {
	        	MCBans.broadcastPlayer( PlayerAdmin, ChatColor.GOLD + "Local bans");
	        	for (int v = 0; v < result.getJSONArray("local").length(); v++) {
	        		MCBans.broadcastPlayer( PlayerAdmin, result.getJSONArray("local").getString(v) );
	        	}
	        }
        } catch (JSONException e) {
            if (result.toString().contains("error")) {
			    if (result.toString().contains("Server Disabled")) {
                    MCBans.broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				    MCBans.broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				    MCBans.log.write("The server API key has been disabled by an MCBans Administrator");
				    MCBans.log.write("To appeal this decision, please contact an administrator");
                    if (MCBans.UsingSpout) {
                        SpoutPlayer myplayer = (SpoutPlayer)MCBans.getServer().getPlayer(PlayerAdmin);
                        GenericPopup somepopup = new GenericPopup();
                        GenericLabel label = new GenericLabel("This server has been disabled by an MCBans Administrator\nIf you feel there was a mistake, please contact an MCBans Administrator.");
                        label.setX(myplayer.getMainScreen().getMaxWidth() / 2 - 160);
                        label.setY(myplayer.getMainScreen().getMaxHeight() / 2 - 15);
                        somepopup.attachWidget(MCBans, label);
                        myplayer.getMainScreen().attachPopupScreen(somepopup);
                    }
                }
            } else {
        	    MCBans.broadcastPlayer( PlayerAdmin, ChatColor.RED + "There was an error while parsing the data! [JSON Error]");
        	    MCBans.log.write("JSON error while trying to parse lookup data!");
            }
        } catch (NullPointerException e) {
        	MCBans.broadcastPlayer( PlayerAdmin, ChatColor.RED + "There was an error while polling the API!");
        	MCBans.log.write("Unable to reach MCBans Master server!");
		}
	}
}