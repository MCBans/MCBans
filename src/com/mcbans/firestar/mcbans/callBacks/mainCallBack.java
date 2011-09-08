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
				MCBans.broadcastBanView( ChatColor.DARK_GREEN + "MCbans version " + response.get("oldVersion") + " now available!" );
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