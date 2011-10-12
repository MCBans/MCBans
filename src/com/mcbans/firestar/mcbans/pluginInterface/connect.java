package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
		int conUserCount = MCBans.getConnectionData(PlayerName);
		int conAllCount = MCBans.getConnectionData("[Global]");
		long timeInMillis = System.currentTimeMillis();
		
		if (MCBans.Settings.getBoolean("throttleUsers") && MCBans.Settings.getInteger("userConnectionTime") > 0 && MCBans.taskID != -1) {
			long maxTime = MCBans.Settings.getInteger("userConnectionTime") * 1000;
			if (conUserCount == 0) {
				long nextReset = timeInMillis + maxTime;
				MCBans.resetTime.put(PlayerName, nextReset);
				MCBans.log.write("resetTime Count: " + MCBans.resetTime.size());
			}
			long checkTime = MCBans.resetTime.get(PlayerName);
			if (checkTime > timeInMillis) {
				if (MCBans.Settings.getInteger("userConnectionLimit") == conUserCount) {
					MCBans.resetTime.put(PlayerName, (timeInMillis + (MCBans.Settings.getInteger("userLockout") * 1000)));
					if (MCBans.Settings.getString("userLockoutMsg") == null) {
						return "Throttled - Connecting too fast";
					} else {
						return MCBans.Settings.getString("userLockoutMsg");
					}
				} else {
					MCBans.setConnectionData(PlayerName, conUserCount++);
				}
				MCBans.log.write("User: " + PlayerName + "|ConCount: " + conUserCount);
				MCBans.log.write("checkTime: " + checkTime + "|timeInMillis: " + timeInMillis);
			}
		}
		if(MCBans.getMode()){
			if(MCBans.Backup.isBanned(PlayerName)){
				s = MCBans.Settings.getString("offlineReason");
			}
			return s;
		}
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put("player", PlayerName);
		url_items.put("playerip", PlayerIP);
		url_items.put("exec", "playerConnect");
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try {
			if(!response.containsKey("banStatus")){
				return null;
			}else{
				ArrayList<String> tempList = new ArrayList<String>();
				switch(responses.get(response.get("banStatus"))){
					case 0:
						if(response.containsKey("altList")){
							if(!response.get("altList").equals("")){
								if(Float.valueOf(response.get("altCount").trim()).floatValue() > MCBans.Settings.getFloat("maxAlts") && MCBans.Settings.getBoolean("enableMaxAlts")) {
									s = MCBans.Language.getFormat( "overMaxAlts" );
								} else { 
									MCBans.broadcastBanView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, response.get("altList") ) );
								}
							}
						}
						if(response.containsKey("is_mcbans_mod")) {
							if(response.get("is_mcbans_mod").equals("y")){
								MCBans.broadcastBanView( ChatColor.AQUA + MCBans.Language.getFormat( "isMCBansMod", PlayerName ));
								tempList.add(ChatColor.AQUA + MCBans.Language.getFormat ("youAreMCBansStaff"));
							}
						}
						if(response.containsKey("disputeCount")){
							if(!response.get("disputeCount").equals("")){
								tempList.add(ChatColor.DARK_RED + response.get("disputeCount") + " open disputes!" );
							}
						}
						if(response.containsKey("connectMessage")){
							if(!response.get("connectMessage").equals("")){
								tempList.add(ChatColor.AQUA + response.get("connectMessage") );
							}
						}
						if (s == null) {
							MCBans.log.write( PlayerName + " has connected!" );
							s = null;
						}
					break;
					case 2:
					case 3:
					case 5:
					case 6:
						s = response.get("banReason");
						MCBans.log.write( PlayerName + " access denied!" );
					break;
					case 4:
					case 1:
						Boolean blockConnection = false;
						if(response.containsKey("altList")){
							if(!response.get("altList").equals("")){
								if(Float.valueOf(response.get("altCount").trim()).floatValue() > MCBans.Settings.getFloat("maxAlts") && MCBans.Settings.getBoolean("enableMaxAlts")) {
									s = MCBans.Language.getFormat( "overMaxAlts" );
									blockConnection = true;
								} else {
									MCBans.broadcastBanView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, response.get("altList") ) );
								}
							}
						}
						if (!blockConnection) {
							if(response.containsKey("disputeCount")){
								if(!response.get("disputeCount").equals("")){
									tempList.add(ChatColor.DARK_RED + response.get("disputeCount") + " open disputes!" );
								}
							}
							if(response.containsKey("connectMessage")){
								if(!response.get("connectMessage").equals("")){
									tempList.add(ChatColor.AQUA + response.get("connectMessage") );
								}
							}
							tempList.add(ChatColor.DARK_RED + "You have bans on record!" );
							if(!response.containsKey("playerRep")){
								MCBans.broadcastBanView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", PlayerName ) );
								MCBans.log.write( PlayerName + " has connected!" );
								s = null;
							}else{
								if(MCBans.Settings.getBoolean("isDebug")){
									System.out.print("Player Rep: "+Float.parseFloat(response.get("playerRep")));
								}
								if( Float.parseFloat( response.get( "playerRep" ) ) > MCBans.Settings.getFloat("minRep") ){
									MCBans.broadcastBanView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", PlayerName ) );
									MCBans.log.write( PlayerName + " has connected!" );
									if(response.containsKey("is_mcbans_mod")) {
										if(response.get("is_mcbans_mod").equals("y")){
											MCBans.broadcastBanView( ChatColor.AQUA + MCBans.Language.getFormat( "isMCBansMod", PlayerName ));
											tempList.add(ChatColor.AQUA + MCBans.Language.getFormat ("youAreMCBansStaff"));
										}
									}
									s = null;
								}else{
									if ( Float.parseFloat( response.get( "playerRep") ) <= 0) {
										s = response.get("banReason");
									} else {
										s = MCBans.Language.getFormat( "underMinRep" );
									}
									MCBans.log.write( PlayerName + " access denied!" );
								}
							}
						}
					break;
				}
				if(s==null && tempList.size()>0){
					if (MCBans.Settings.getBoolean("throttleUsers")) {
						// +1 to the user's connection count
						MCBans.setConnectionData(PlayerName, conUserCount + 1);
					}
					if (MCBans.Settings.getBoolean("throttleAll")) {
						// +1 to the total connection count
						MCBans.setConnectionData("[Global]", conAllCount + 1);
					}
					Player target = MCBans.getServer().getPlayer(PlayerName);
					if( MCBans.Permissions.isAllow( target.getWorld().getName(), target.getName(), "ban.view" ) ){
						if (MCBans.Settings.getBoolean("mcbansUnconfigured")) {
							target.sendMessage( MCBans.Settings.getString("prefix") + " Thank you for installing MCBans on your server! Please edit the settings.yml file located in the plugins/mcbans directory and customize it to your needs. This notice will disappear after the new settings take effect.");
						}
					}
					MCBans.joinMessages.put( PlayerName, tempList);
				}
			}
			return s;
		} catch (NullPointerException e) {
			return s;
		}
	}
}