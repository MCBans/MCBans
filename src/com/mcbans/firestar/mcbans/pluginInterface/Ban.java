package com.mcbans.firestar.mcbans.pluginInterface;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;

import fr.neatmonster.nocheatplus.players.NCPPlayer;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.*;
public class Ban implements Runnable {
	private BukkitInterface MCBans;
	private String PlayerName = null;
	private String PlayerIP = null;
	private String PlayerAdmin = null;
	private String Reason = null;
	private String Action = null;
	private int rollbackTime = 20;
	private String Duration = null;
	private String Measure = null;
	private boolean Rollback = false;
	private String Badword = null;
	private JSONObject ActionData = new JSONObject();
	private HashMap<String, Integer> responses = new HashMap<String, Integer>();
	
	public Ban(BukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration, String measure){
		MCBans = p;
		PlayerName = playerName;
		PlayerIP = playerIP;
		PlayerAdmin = playerAdmin;
		Reason = reason;
		Duration = duration;
		Rollback = MCBans.Settings.getBoolean("rollbackOnBan");
		rollbackTime = MCBans.Settings.getInteger("backDaysAgo");
		Measure = measure;
		Action = action;
		responses.put( "globalBan", 0 );
		responses.put( "localBan", 1 );
		responses.put( "tempBan", 2 );
		responses.put( "unBan", 3 );
	}
	
	public Ban(BukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration, String measure, JSONObject actionData, boolean rollback){
		MCBans = p;
		PlayerName = playerName;
		PlayerIP = playerIP;
		PlayerAdmin = playerAdmin;
		Reason = reason;
		Rollback = rollback;
		rollbackTime = MCBans.Settings.getInteger("backDaysAgo");
		Duration = duration;
		Measure = measure;
		Action = action;
		ActionData = actionData;
		responses.put( "globalBan", 0 );
		responses.put( "localBan", 1 );
		responses.put( "tempBan", 2 );
		responses.put( "unBan", 3 );
	}
	public Ban(BukkitInterface p, String action, String playerName, String playerIP, String playerAdmin, String reason, String duration, String measure, JSONObject actionData, int rollback){
		MCBans = p;
		PlayerName = playerName;
		PlayerIP = playerIP;
		PlayerAdmin = playerAdmin;
		Reason = reason;
		Rollback = true;
		rollbackTime = rollback;
		Duration = duration;
		Measure = measure;
		Action = action;
		ActionData = actionData;
		responses.put( "globalBan", 0 );
		responses.put( "localBan", 1 );
		responses.put( "tempBan", 2 );
		responses.put( "unBan", 3 );
	}
	public void kickPlayer( String playerToKick, final String kickString ){
		final Player target = MCBans.getServer().getPlayer(playerToKick);
		if(target!=null){
			MCBans.getServer().getScheduler().scheduleSyncDelayedTask(MCBans, new Runnable() {
			    public void run() {
			    	target.kickPlayer(kickString);
			    }
			}, 1L);
		}
	}
	public void run(){
		while(MCBans.notSelectedServer){
			//waiting for server select
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				if(MCBans.Settings.getBoolean("isDebug")){
					e.printStackTrace();
				}
			}
		}
		try{
			if(responses.containsKey(Action)){
				switch(responses.get(Action)){
					case 0:
						globalBan();
					break;
					case 1:
						localBan();
					break;
					case 2:
						tempBan();
					break;
					case 3:
						unBan();
					break;
				}
			}else{
				MCBans.log("Error, caught invalid action! Another plugin using mcbans improperly?");
			}
		} catch (NullPointerException e) {
			if(MCBans.Settings.getBoolean("isDebug")){
				e.printStackTrace();
			}
		}
	}
	public void unBan( ){
		JsonHandler webHandle = new JsonHandler( MCBans );
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
				OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
				if(d.isBanned()){
					d.setBanned(false);
				}
				MCBans.log( PlayerAdmin + " unbanned " + PlayerName + "!" );
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.GREEN + MCBans.Language.getFormat( "unBanMessageSuccess", PlayerName, PlayerAdmin ) );
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageError", PlayerName, PlayerAdmin ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageGroup", PlayerName, PlayerAdmin ) );
			}else if(response.get("result").equals("n")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "unBanMessageNot", PlayerName, PlayerAdmin ) );
			}
			MCBans.log( PlayerAdmin + " tried to unban " + PlayerName + "!" );
		} catch (NullPointerException e) {
			if(MCBans.Settings.getBoolean("isDebug")){
				e.printStackTrace();
			}
		}
	}
	public void localBan( ){
		JsonHandler webHandle = new JsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "playerip", PlayerIP );
		url_items.put( "reason", Reason );
		url_items.put( "admin", PlayerAdmin );
		if(MCBans.logblock!=null){
			if(Rollback){
				rollback();
			}
		}
		if(ActionData!=null){
			url_items.put( "actionData", ActionData.toString());
		}
		url_items.put( "exec", "localBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
				OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
				if(!d.isBanned()){
					d.setBanned(true);
				}
				this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
				//MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log( PlayerName + " has been banned with a local type ban [" + Reason + "] [" + PlayerAdmin + "]!" );
				this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
				MCBans.broadcastAll( ChatColor.GREEN + MCBans.Language.getFormat( "localBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("a")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "localBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}
			MCBans.log( PlayerAdmin + " has tried to ban " + PlayerName + " with a local type ban ["+Reason+"]!" );
		} catch (NullPointerException e) {
			MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
			OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
			if(!d.isBanned()){
				d.setBanned(true);
			}
			this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
			if(MCBans.Settings.getBoolean("isDebug")){
				e.printStackTrace();
			}
		}
	}
	
	public void globalBan( ){
		JsonHandler webHandle = new JsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "playerip", PlayerIP );
		url_items.put( "reason", Reason );
		url_items.put( "admin", PlayerAdmin );
		if(MCBans.noCheatPlus!=null){
			boolean foundMatch = false;
			try {
				Pattern regex = Pattern.compile("(fly|hack|nodus|glitch|exploit|NC)");
				Matcher regexMatcher = regex.matcher(Reason);
				foundMatch = regexMatcher.find();
			} catch (PatternSyntaxException ex) {
			}
			if(MCBans.getServer().getPlayer(PlayerName)!=null && foundMatch == true){
				JSONObject tmp = new JSONObject();
				final NCPPlayer player = NCPPlayer.getPlayer(MCBans.getServer().getPlayer(PlayerName));
				try {
					for(Entry<String, Object> s:player.collectData().entrySet()){
						tmp.put(s.getKey(), s.getValue());
					}
					ActionData.put("nocheatplus", tmp);
				} catch (JSONException e) {
					if(MCBans.Settings.getBoolean("isDebug")){
						e.printStackTrace();
					}
				}
			}
		}
		if(MCBans.logblock!=null){
			boolean foundMatch = false;
			try {
				Pattern regex = Pattern.compile("(grief|broke|destroy)");
				Matcher regexMatcher = regex.matcher(Reason);
				foundMatch = regexMatcher.find();
			} catch (PatternSyntaxException ex) {
			}
			if(foundMatch){
				String[] worlds = MCBans.Settings.getString("affectedWorlds").split(",");
				JSONObject Out = new JSONObject();
				for(String world: worlds){
					QueryParams params = new QueryParams(MCBans.logblock);
					params.setPlayer(PlayerName);
					params.bct = BlockChangeType.ALL;
					params.limit = -1;
					params.world = MCBans.getServer().getWorld(world);
					params.needDate = true;
					params.needType = true;
					params.needData = true;
					params.needPlayer = true;
					params.needCoords = true;
					params.needSignText = true;
					JSONObject tmpOut = new JSONObject();
					int increment = 0;
					try {
						for (BlockChange bc : MCBans.logblock.getBlockChanges(params)){
							try {
								JSONObject tmp = new JSONObject();
								tmp.put("d", String.valueOf(bc.date));
								if(bc.loc!=null){
									tmp.put("x", String.valueOf(bc.loc.getX()));
									tmp.put("y", String.valueOf(bc.loc.getY()));
									tmp.put("z", String.valueOf(bc.loc.getZ()));
								}
								tmp.put("b", String.valueOf(bc.data));
								tmp.put("c", String.valueOf(bc.ca));
								if(bc.signtext!=null){
									tmp.put("signText", "\""+bc.signtext+"\"");
								}
								tmp.put("t", String.valueOf(bc.type));
								tmp.put("r", String.valueOf(bc.replaced));
								tmp.put("p", String.valueOf(bc.playerName));
								tmpOut.put(String.valueOf(increment),tmp);
								increment++;
							} catch (JSONException e) {
								if(MCBans.Settings.getBoolean("isDebug")){
									e.printStackTrace();
								}
							} catch (NullPointerException en){
								if(MCBans.Settings.getBoolean("isDebug")){
									en.printStackTrace();
								}
							}
						}
					} catch (SQLException e) {
						if(MCBans.Settings.getBoolean("isDebug")){
							e.printStackTrace();
						}
					} catch (NullPointerException en){
						if(MCBans.Settings.getBoolean("isDebug")){
							en.printStackTrace();
						}
					}
					try {
						Out.put(world, tmpOut);
					} catch (JSONException e) {
						if(MCBans.Settings.getBoolean("isDebug")){
							e.printStackTrace();
						}
					}
				}
				try {
					ActionData.put("logblock", Out);
				} catch (JSONException e) {
					if(MCBans.Settings.getBoolean("isDebug")){
						e.printStackTrace();
					}
				}
			}
			if(Rollback){
				rollback();
			}
		}
		if(ActionData.length()>0){
			url_items.put( "actionData", ActionData.toString());
		}
		url_items.put( "exec", "globalBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
				OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
				if(!d.isBanned()){
					d.setBanned(true);
				}
				this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
				//MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log( PlayerName + " has been banned with a global type ban [" + Reason + "] [" + PlayerAdmin + "]!" );
				this.kickPlayer(PlayerName,MCBans.Language.getFormat( "globalBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
				MCBans.broadcastAll( ChatColor.GREEN + MCBans.Language.getFormat( "globalBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("w")){
				Badword = response.get("word");
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageWarning", PlayerName, PlayerAdmin, Reason, PlayerIP, Badword ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("a")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "globalBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}
			MCBans.log( PlayerAdmin + " has tried to ban " + PlayerName + " with a global type ban ["+Reason+"]!" );
		} catch (NullPointerException e) {
			MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
			OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
			if(!d.isBanned()){
				d.setBanned(true);
			}
			this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
			if(MCBans.Settings.getBoolean("isDebug")){
				e.printStackTrace();
			}
		}
	}
	public void tempBan( ){
		JsonHandler webHandle = new JsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put( "player", PlayerName );
		url_items.put( "playerip", PlayerIP );
		url_items.put( "reason", Reason );
		url_items.put( "admin", PlayerAdmin );
		url_items.put( "duration", Duration );
		url_items.put( "measure", Measure );
		if(MCBans.logblock!=null && MCBans.Settings.getBoolean("enableTempBanRollback")){
			if(Rollback){
				rollback();
			}
		}
		if(ActionData!=null){
			url_items.put( "actionData", ActionData.toString());
		}
		url_items.put( "exec", "tempBan" );
		HashMap<String, String> response = webHandle.mainRequest(url_items);
		try{
			if(!response.containsKey("result")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
				OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
				if(!d.isBanned()){
					d.setBanned(true);
				}
				this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
				//MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}
			if(response.get("result").equals("y")){
				MCBans.log( PlayerName + " has been banned with a temp type ban [" + Reason + "] [" + PlayerAdmin + "]!" );
				this.kickPlayer(PlayerName,MCBans.Language.getFormat( "tempBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
				MCBans.broadcastAll( ChatColor.GREEN + MCBans.Language.getFormat( "tempBanMessageSuccess", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
				return;
			}else if(response.get("result").equals("e")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageError", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("s")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageGroup", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}else if(response.get("result").equals("a")){
				MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + MCBans.Language.getFormat( "tempBanMessageAlready", PlayerName, PlayerAdmin, Reason, PlayerIP ) );
			}
			MCBans.log( PlayerAdmin + " has tried to ban " + PlayerName + " with a temp type ban ["+Reason+"]!" );
		} catch (NullPointerException e) {
			MCBans.broadcastPlayer( PlayerAdmin, ChatColor.DARK_RED + " MCBans down, adding local ban, unban with /pardon");
			OfflinePlayer d = MCBans.getServer().getOfflinePlayer(PlayerName);
			if(!d.isBanned()){
				d.setBanned(true);
			}
			this.kickPlayer(PlayerName,MCBans.Language.getFormat( "localBanMessagePlayer", PlayerName, PlayerAdmin, Reason, PlayerIP ));
			if(MCBans.Settings.getBoolean("isDebug")){
				e.printStackTrace();
			}
		}
	}
	public void rollback(){
		String[] worlds = MCBans.Settings.getString("affectedWorlds").split(",");
		Player h = MCBans.getServer().getPlayer(PlayerAdmin);
		if(h==null){
			h = MCBans.getServer().getPlayer(PlayerName);
		}
		if(h!=null){
			for(String world: worlds){
				QueryParams params = new QueryParams(MCBans.logblock);
				params.setPlayer(PlayerName);
				params.since = (rollbackTime*MCBans.Settings.getInteger("backDaysAgo"));
				params.world = MCBans.getServer().getWorld(world);
				params.silent = false;
				try {
					MCBans.logblock.getCommandsHandler().new CommandRollback((CommandSender) h, params, true);
					MCBans.broadcastPlayer( PlayerAdmin, ChatColor.GREEN + "Rollback successful!");
				} catch (Exception e) {
					MCBans.broadcastPlayer( PlayerAdmin, ChatColor.RED + "Unable to rollback player!");
					if(MCBans.Settings.getBoolean("isDebug")){
						e.printStackTrace();
					}
				}
			}
		}else{
			MCBans.log( PlayerAdmin + " has tried to rollback " + PlayerName + " but neither were online, so rollback was ignored (run this command seperately!)!" );
		}
	}
}