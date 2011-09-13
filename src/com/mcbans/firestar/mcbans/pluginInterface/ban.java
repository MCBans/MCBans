package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;

public class ban extends Thread {
	private bukkitInterface MCBans;
	private String PlayerName = null;
	private String PlayerIP = null;
	private String PlayerAdmin = null;
	private String Reason = null;
	private String Action = null;
	private String Duration = null;
	private String Measure = null;
	private HashMap<String, Integer> responses = new HashMap<String, Integer>();
	public ban( bukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration, String measure ){
		MCBans = p;
		PlayerName = playerName;
		PlayerIP = playerIP;
		PlayerAdmin = playerAdmin;
		Reason = reason;
		Duration = duration;
		Measure = measure;
		Action = action;
		responses.put( "globalBan", 0 );
		responses.put( "localBan", 1 );
		responses.put( "tempBan", 2 );
		responses.put( "unBan", 3 );
	}
	public void run(){
		try{
			if(responses.containsKey(Action)){
				switch(responses.get(Action)){
					case 0:
						if(MCBans.getMode()){
							if(MCBans.Backup.add(PlayerName)){
								if (MCBans.getServer().getPlayer(PlayerName) != null) {
									MCBans.getServer().getPlayer(PlayerName).kickPlayer(MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
					            }
								MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
							}else{
								MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
							}
							return;
						}
						globalBan();
					break;
					case 1:
						if(MCBans.getMode()){
							if(MCBans.Backup.add(PlayerName)){
								if (MCBans.getServer().getPlayer(PlayerName) != null) {
									MCBans.getServer().getPlayer(PlayerName).kickPlayer(MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
					            }
								MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
							}else{
								MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
							}
							return;
						}
						localBan();
					break;
					case 2:
						if(MCBans.getMode()){
							MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + "Offline Mode! This will not work." );
						}
						tempBan();
					break;
					case 3:
						if(MCBans.getMode()){
							if(MCBans.Backup.remove(PlayerName)){
								MCBans.log.write( PlayerAdmin + " unbanned " + PlayerName + "!" );
								MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageSuccess", PlayerName, PlayerAdmin ) );
							}else{
								MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageNot", PlayerName, PlayerAdmin ) );
							}
							return;
						}
						unBan();
					break;
				}
			}else{
				MCBans.log.write("Error, caught invalid action! Another plugin using mcbans improperly?");
			}
		} catch (NullPointerException e) {
		}
	}
	public void unBan( ){
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "admin", PlayerAdmin );
		url_items.put( "exec", "unBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageError", PlayerName, PlayerAdmin ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log.write( PlayerAdmin + " unbanned " + PlayerName + "!" );
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageSuccess", PlayerName, PlayerAdmin ) );
				MCBans.Backup.remove(PlayerName);
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageError", PlayerName, PlayerAdmin ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageGroup", PlayerName, PlayerAdmin ) );
			}else if(response.get("result").equals("n")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageNot", PlayerName, PlayerAdmin ) );
			}
			MCBans.log.write( PlayerAdmin + " tried to unban " + PlayerName + "!" );
		} catch (NullPointerException e) {
		}
	}
	public void localBan( ){
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "playerip", PlayerIP );
		url_items.put( "reason", Reason );
		url_items.put( "admin", PlayerAdmin );
		url_items.put( "exec", "localBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log.write( PlayerName + " has been banned with a local type ban [" + Reason + "] [" + PlayerAdmin + "]!" );
				if (MCBans.getServer().getPlayer(PlayerName) != null) {
					MCBans.getServer().getPlayer(PlayerName).kickPlayer(MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
	            }
				MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				MCBans.Backup.add(PlayerName);
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("a")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}
			MCBans.log.write( PlayerAdmin + " has tried to ban " + PlayerName + " with a local type ban ["+Reason+"]!" );
		} catch (NullPointerException e) {
		}
	}
	public void globalBan( ){
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "playerip", PlayerIP );
		url_items.put( "reason", Reason );
		url_items.put( "admin", PlayerAdmin );
		url_items.put( "exec", "globalBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log.write( PlayerName + " has been banned with a global type ban [" + Reason + "] [" + PlayerAdmin + "]!" );
				if (MCBans.getServer().getPlayer(PlayerName) != null) {
					MCBans.getServer().getPlayer(PlayerName).kickPlayer(MCBans.Language.getFormat( "globalBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
	            }
				MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				MCBans.Backup.add(PlayerName);
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("w")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageWarning", PlayerName, PlayerAdmin, Reason, PlayerIP, response.get("word") ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("a")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}
			MCBans.log.write( PlayerAdmin + " has tried to ban " + PlayerName + " with a global type ban ["+Reason+"]!" );
		} catch (NullPointerException e) {
		}
	}
	public void tempBan( ){
		jsonHandler webHandle = new jsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "playerip", PlayerIP );
		url_items.put( "reason", Reason );
		url_items.put( "admin", PlayerAdmin );
		url_items.put( "duration", Duration );
		url_items.put( "measure", Measure );
		url_items.put( "exec", "tempBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log.write( PlayerName + " has been banned with a temp type ban [" + Reason + "] [" + PlayerAdmin + "]!" );
				if (MCBans.getServer().getPlayer(PlayerName) != null) {
					MCBans.getServer().getPlayer(PlayerName).kickPlayer( MCBans.Language.getFormat( "tempBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
	            }
				MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("a")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}
			MCBans.log.write( PlayerAdmin + " has tried to ban " + PlayerName + " with a temp type ban ["+Reason+"]!" );
		} catch (NullPointerException e) {
		}
	}
}