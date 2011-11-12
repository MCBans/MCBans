package com.mcbans.firestar.mcbans.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.ban;
import com.mcbans.firestar.mcbans.pluginInterface.kick;
import com.mcbans.firestar.mcbans.pluginInterface.lookup;
import com.mcbans.firestar.mcbans.pluginInterface.playerSet;


public class commandHandler{
	private bukkitInterface MCBans;
	private Settings Config;
	//private String[] protectedGroups;
	private HashMap<String, Integer> commandList = new HashMap<String, Integer>();
	public commandHandler( Settings cf, bukkitInterface p ){
		MCBans = p;
		Config=cf;
		//protectedGroups = MCBans.Settings.getString("protected").split(",");
		commandList.put("ban", 0);
		commandList.put("tempban", 1);
		commandList.put("unban", 2);
		commandList.put("kick", 3);
		commandList.put("lookup", 4);
		commandList.put("lup", 4);
		commandList.put("mcbans", 5);
	}
	public boolean execCommand(String command, String[] args, CommandSender from){
		lookup lookupControl = null;
		playerSet mcbansControl = null;
		String CommandSend = "";
		String PlayerIP = "";
		kick kickControl = null;
		boolean commandSet = false;
		boolean isPlayer = false;
		String inWorld = "";
		String reasonString = "";
		ban banControl = null;
		if (from instanceof Player) {
            Player player = (Player) from;
            CommandSend = player.getName();
            isPlayer = true;
            inWorld = player.getWorld().getName();
        } else {
            CommandSend = "Console";
            isPlayer = false;
        }
		if(args.length>=1){
			Player target = MCBans.getServer().getPlayer(args[0]);
			if( target!=null ){
				PlayerIP = target.getAddress().getAddress().getHostAddress();
			}
		}
		switch(commandList.get(command.toLowerCase())){
			case 0:
				// Check if Global or Local
				if(args.length<1){
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					return true;
				}
				if(args.length>=2){
					if(args[1].equalsIgnoreCase("g")){
						if(args.length<3){
							MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
							return true;
						}
						reasonString = getReason(args,"",2);
						// Check Permissions
						if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.global") || !isPlayer){
							banControl = new ban( MCBans, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
							banControl.start();
						}else{
							MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
							MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
						}
					}else{
						if(args.length<1){
							MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
							return true;
						}
						if(args.length==1){
							reasonString = Config.getString("defaultLocal");
						}else{
							reasonString = getReason(args,"",1);
						}
						// Check Permissions
						if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.local") || !isPlayer){
							banControl = new ban( MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
							banControl.start();
						}else{
							MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
							MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
						}
					}
				}else{
					if(args.length<1){
						MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
						return true;
					}
					if(args.length==1){
						reasonString = Config.getString("defaultLocal");
					}else{
						reasonString = getReason(args,"",1);
					}
					// Check Permissions
					if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.local") || !isPlayer){
						banControl = new ban( MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
						banControl.start();
					}else{
						MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
						MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
					}
				}
				commandSet = true;
			break;
			case 1:
				if(args.length<3){
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					return true;
				}
				if(args.length==3){
					reasonString = Config.getString("defaultTemp");
				}else{
					reasonString = getReason(args,"",3);
				}
				// Check Permissions
				if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.temp")){
					banControl = new ban( MCBans, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[1], args[2] );
					banControl.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case 2:
				if(args.length<1){
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					return true;
				}
				// Check Permissions
				if(MCBans.Permissions.isAllow( inWorld, CommandSend, "unban") || !isPlayer){
					banControl = new ban( MCBans, "unBan", args[0], "", CommandSend, "", "", "" );
					banControl.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case 3:
				if(args.length<1){
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					return true;
				}
				if(args.length==1){
					reasonString = Config.getString("defaultKick");
				}else{
					reasonString = getReason(args,"",2);
				}
				// Check Permissions
				if(MCBans.Permissions.isAllow( inWorld, CommandSend, "kick") || !isPlayer){
					kickControl = new kick( Config, MCBans, args[0], CommandSend, getReason(args,"",1) );
					kickControl.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case 4:
				if(args.length<1){
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					return true;
				}
				// Check Permissions
				if(MCBans.Permissions.isAllow( inWorld, CommandSend, "lookup") || !isPlayer){
					lookupControl = new lookup( MCBans, args[0], CommandSend );
					lookupControl.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case 5:
				if(args.length<1){
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					return true;
				}
				if(args[0].equalsIgnoreCase("online") || args[0].equalsIgnoreCase("offline") || args[0].equalsIgnoreCase("status")){
					if(MCBans.Permissions.isAllow( inWorld, CommandSend, "mode") || !isPlayer){
						if(args[0].equalsIgnoreCase("online")){
							MCBans.broadcastPlayer( CommandSend, ChatColor.LIGHT_PURPLE + "Running online mode!" );
							MCBans.setMode(false);
							return true;
						}else if(args[0].equalsIgnoreCase("offline")){
							MCBans.broadcastPlayer( CommandSend, ChatColor.LIGHT_PURPLE + "Running offline mode!" );
							MCBans.setMode(true);
							return true;
						}else if(args[0].equalsIgnoreCase("status")){
							if(MCBans.getMode()){
								MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE+ "Running in [" + ChatColor.DARK_RED + "offline" + ChatColor.WHITE + "] mode!" );
							}else{
								MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE+ "Running in [" + ChatColor.DARK_GREEN + "online" + ChatColor.WHITE + "] mode!" );
							}
							MCBans.setMode(true);
							return true;
						}
					}else{
						MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
						return true;
					}
				}
				commandSet = true;
			break;
		}
		return commandSet;
	}
	private String getReason(String[] args, String reason, int start) {
        for (int x = start; x < args.length; x++) {
            reason += reason.equalsIgnoreCase("") ? args[x] : " " + args[x];
        }
        return reason;
    }
}