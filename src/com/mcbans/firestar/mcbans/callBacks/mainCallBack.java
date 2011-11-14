package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;


public class mainCallBack extends Thread {
	private final bukkitInterface MCBans;
	public mainCallBack(bukkitInterface p){
		MCBans = p;
	}
	@Override
	public void run(){
		int callBackInterval = MCBans.Settings.getInteger("callBackInterval");
		if(callBackInterval<600000){
			callBackInterval=600000;
		}
		while(true){
			this.mainRequest();
			try {
				Thread.sleep(callBackInterval);
			} catch (InterruptedException e) {
			}
		}
	}
	private void mainRequest(){
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "maxPlayers", String.valueOf( MCBans.getServer().getMaxPlayers() ) );
		url_items.put( "playerList", this.playerList() );
		url_items.put( "version", MCBans.getDescription().getVersion() );
		url_items.put( "exec", "callBack" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		if(response.containsKey("oldVersion")){
			if(!response.get("oldVersion").equals("")){
				String oldVersion = response.get("oldVersion");
				// Version replies can be:
				// 3.3imp, 3.3
				// The former being the update is important and should be downloaded ASAP, the latter is that the update does not contain a critical fix/patch
				if (oldVersion.endsWith("imp")) {
					oldVersion = oldVersion.replace("imp", "");
					MCBans.broadcastBanView( ChatColor.BLUE + "A newer version of MCBans (" + oldVersion + ") is now available!");
					MCBans.broadcastBanView( ChatColor.RED + "This is an important/critical update.");
				} else {
					MCBans.broadcastBanView( ChatColor.BLUE + "A newer version of MCBans (" + oldVersion + ") is now available!");
				}
			}
		}
		if (response.containsKey("error")) {
			String error = response.get("error");
			if (error == "v2: Server Disabled, ABUSE DETECTED.") {
				MCBans.broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				MCBans.broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				MCBans.log.write("The server API key has been disabled by an MCBans Administrator");
				MCBans.log.write("To appeal this decision, please contact an administrator");
				MCBans.setMode(true);
			} else if (error == "v2: Server with that api key not found.") {
				MCBans.broadcastBanView( ChatColor.RED + "Invalid MCBans.jar!");
				MCBans.broadcastBanView( "The API key inside the current MCBans.jar is invalid. Please re-download the plugin from myserver.mcbans.com");
				MCBans.log.write("Invalid MCBans.jar - Please re-download from myserver.mcbans.com!");
				MCBans.getServer().getPluginManager().disablePlugin(MCBans.pluginInterface("mcbans"));
			} else {
				MCBans.broadcastBanView( ChatColor.RED + "Unexpected reply from MCBans API!");
				MCBans.log.write("API returned an invalid error:");
				MCBans.log.write("MCBans said: " + error);
			}
		}
		/*if(response.containsKey("newMessages")){
			if(!response.get("newMessages").equals("")){
				String[] Players = response.get("newMessages").split(",");
				for( String player: Players ){
					String[] params = player.split(":");
					MCBans.broadcastPlayer(params[0], ChatColor.GOLD + MCBans.Language.getFormatCount( "newMessage", params[1]) );
				}
			}
		}*/
	}
	private String playerList(){
		StringBuilder playerList=new StringBuilder();
		for(Player player: MCBans.getServer().getOnlinePlayers()){
			if(playerList.length()>0){
				playerList.append(",");
			}
			playerList.append(player.getName());
		}
		return playerList.toString();
	}
}