package com.mcbans.mcbans.callBacks;

import com.mcbans.mcbans.BukkitInterface;
import com.mcbans.mcbans.request.JsonHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;


public class MainCallBack extends Thread {
	private final BukkitInterface MCBans;
	public MainCallBack(BukkitInterface p){
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
		JsonHandler webHandle = new JsonHandler( MCBans );
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
		if (MCBans.hasErrored(response)) {
			return;
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