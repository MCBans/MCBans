package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;

public class connect{
	private bukkitInterface MCBans;
	private HashMap<String, Integer> responses = new HashMap<String, Integer>();
	public connect( bukkitInterface p ){
		responses.put("n", 0);
		responses.put("g", 1);
		responses.put("s", 2);
		responses.put("i", 3);
		responses.put("b", 4);
		responses.put("t", 5);
		responses.put("l", 6);
		MCBans = p;
	}
	public String exec( String PlayerName, String PlayerIP ){
		String s = null;
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put("player", PlayerName);
		url_items.put("playerip", PlayerIP);
		url_items.put("exec", "playerConnect");
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		if(!response.containsKey("banStatus")){
			return null;
		}else{
			switch(responses.get(response.get("banStatus"))){
				case 0:
					if(response.containsKey("altList")){
						if(!response.get("altList").equals("")){
							MCBans.broadcastBanView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, response.get("altList") ) );
						}
					}
					MCBans.log.write( PlayerName + " has connected!" );
					s = null;
				break;
				case 2:
				case 3:
				case 5:
				case 6:
					s = response.get("banReason");
					MCBans.log.write( PlayerName + " access denied!" );
				break;
				case 1:
				case 4:
					if(response.containsKey("altList")){
						if(!response.get("altList").equals("")){
							MCBans.broadcastBanView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, response.get("altList") ) );
						}
					}
					if(!response.containsKey("playerRep")){
						MCBans.broadcastBanView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", PlayerName ) );
						MCBans.log.write( PlayerName + " has connected!" );
						s = null;
					}else{
						if(MCBans.Settings.getBoolean("isDebug")){
							System.out.print("Player Rep: "+Float.parseFloat(response.get("playerRep")));
						}
						if( Float.parseFloat( response.get( "playerRep" ) ) >= MCBans.Settings.getFloat("minRep") ){
							MCBans.broadcastBanView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", PlayerName ) );
							MCBans.log.write( PlayerName + " has connected!" );
							s = null;
						}else{
							s = MCBans.Language.getFormat( "underMinRep" );
						}
					}
				break;
			}
		}
		return s;
	}
}