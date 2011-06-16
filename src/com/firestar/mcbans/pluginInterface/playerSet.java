package com.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;


import com.firestar.mcbans.bukkitInterface;
import com.firestar.mcbans.request.jsonHandler;

public class playerSet extends Thread {
	private bukkitInterface MCBans;
	private String PlayerName;
	private String ConfirmCode;
	public playerSet( bukkitInterface p, String playerName, String confirmCode ){
		MCBans = p;
		PlayerName = playerName;
		ConfirmCode = confirmCode;
	}
	public void run(){
		HashMap<String, String> url_items = new HashMap<String, String>();
		jsonHandler webHandle = new jsonHandler( MCBans );
        url_items.put("player", PlayerName);
        url_items.put("string", ConfirmCode);
        url_items.put("exec", "playerSet");
        HashMap<String, String> response = webHandle.mainRequest(url_items);
        if(response.containsKey("result")){
        	if(!response.get("result").equals("n")){
        		MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + "Welcome to MCBans: " + response.get("result") );
        	}else{
        		MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_RED + "Error, could not authenticate, sure you signed up?" );
        	}
        }else{
        	MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_RED + "Error, could not authenticate, did you enter it correctly?" );
        }
	}
}