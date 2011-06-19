package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.request.jsonHandler;

public class messager extends Thread {
	private bukkitInterface MCBans;
	private String PlayerName;
	private String TargetName;
	private String MessageID;
	private String Message;
	private String Action;
	private HashMap<String, Integer> responses = new HashMap<String, Integer>();
	public messager( bukkitInterface p, String action, String playerName, String targetName, String messageID, String message ){
		MCBans = p;
		PlayerName = playerName;
		MessageID = messageID;
		Action = action;
		TargetName = targetName;
		Message = message;
		responses.put( "inbox", 0 );
		responses.put( "read", 1 );
		responses.put( "send", 2 );
		responses.put( "block", 3 );
		responses.put( "unblock", 4 );
	}
	public void run(){
		HashMap<String, String> url_items = new HashMap<String, String>();
		HashMap<String, String> response = null;
		jsonHandler webHandle = new jsonHandler( MCBans );
		switch(responses.get(Action)){
			case 0:
				url_items.put("player", PlayerName);
		        url_items.put("exec", "getInbox");
		        response = webHandle.mainRequest(url_items);
		        if(response.containsKey("result")){
					if(response.get("result").equals("n")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GRAY + MCBans.Language.getFormat( "inboxEmpty", PlayerName ) );
						return;
					}else if(response.get("result").equals("y")){
						String[] inboxValues = response.get("messages").split(";");
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + MCBans.Language.getFormat( "InboxHeader", PlayerName ) );
						for(String message : inboxValues){
							MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + message );
						}
					}
		        }
			break;
			case 1:
				url_items.put("player", PlayerName);
				if(MessageID.equals("")){
					url_items.put("exec", "getNewMessage");
				}else{
					url_items.put("message", MessageID);
					url_items.put("exec", "getMessage");
				}
		        response = webHandle.mainRequest(url_items);
		        if(response.containsKey("result")){
					if(response.get("result").equals("n")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GRAY + MCBans.Language.getFormat( "messageNone", PlayerName ) );
						return;
					}else if(response.get("result").equals("y")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + MCBans.Language.getFormatMessageView( "messageView", response.get("sender"),response.get("date"), response.get("message") ) );
					}
		        }
			break;
			case 2:
				url_items.put("player", PlayerName);
				url_items.put("target", TargetName);
				url_items.put("message", Message);
				url_items.put("exec", "sendMessage");
		        response = webHandle.mainRequest(url_items);
		        if(response.containsKey("result")){
					if(response.get("result").equals("n")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_RED + MCBans.Language.getFormat( "messageError", PlayerName ) );
						return;
					}else if(response.get("result").equals("y")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + MCBans.Language.getFormat( "messageSent" ) );
					}
		        }
			break;
			case 3:
				url_items.put("player", PlayerName);
				url_items.put("target", TargetName);
				url_items.put("exec", "playerBlock");
		        response = webHandle.mainRequest(url_items);
		        if(response.containsKey("result")){
					if(response.get("result").equals("a")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_RED + MCBans.Language.getFormat( "blockAlready", PlayerName ) );
						return;
					}else if(response.get("result").equals("y")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + MCBans.Language.getFormat( "blockSuccess", TargetName ) );
					}
		        }
			break;
			case 4:
				url_items.put("player", PlayerName);
				url_items.put("target", TargetName);
				url_items.put("exec", "playerUnBlock");
		        response = webHandle.mainRequest(url_items);
		        if(response.containsKey("result")){
					if(response.get("result").equals("n")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_RED + MCBans.Language.getFormat( "blockNot", PlayerName ) );
						return;
					}else if(response.get("result").equals("y")){
						MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + MCBans.Language.getFormat( "unBlockSuccess", TargetName ) );
					}
		        }
			break;
		}
	}
}