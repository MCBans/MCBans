package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;


import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;

public class disconnect extends Thread {
	private bukkitInterface MCBans;
	private String PlayerName;
	
	public disconnect( bukkitInterface p, String Player ){
		MCBans = p;
		PlayerName=Player;
	}
	@Override
	public void run() {
		MCBans.log.write( PlayerName + " has disconnected!" );
		jsonHandler webhandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put("player", PlayerName);
		url_items.put("exec", "playerDisconnect");
		webhandle.mainRequest(url_items);
	}
}