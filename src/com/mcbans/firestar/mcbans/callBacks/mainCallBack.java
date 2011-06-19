package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;


public class mainCallBack extends Thread {
	private bukkitInterface MCBans = null;
	public mainCallBack(bukkitInterface p){
		MCBans = p;
	}
	public void run(){
		int callBacInterval = MCBans.Settings.getInteger("callBackInterval");
		while(true){
			this.mainRequest();
			try {
				Thread.sleep(callBacInterval);
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
				MCBans.broadcastBanView( ChatColor.DARK_GREEN + "MCbans version " + response.get("oldVersion") + " now available!" );
			}
		}
		if(response.containsKey("newMessages")){
			if(!response.get("newMessages").equals("")){
				String[] Players = response.get("newMessages").split(",");
				for( String player: Players ){
					String[] params = player.split(":");
					MCBans.broadcastPlayer(params[0], ChatColor.GOLD + MCBans.Language.getFormatCount( "newMessage", params[1]) );
				}
			}
		}
	}
	private String playerList(){
		String playerList="";
		for(Player player: MCBans.getServer().getOnlinePlayers()){
			if(playerList.equals("")){
				playerList = player.getName();
			}else{
				playerList += "," + player.getName();
			}
		}
		return playerList;
	}
}