package com.mcbans.firestar.mcbans.commands;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;
import com.mcbans.firestar.mcbans.pluginInterface.Kick;
import com.mcbans.firestar.mcbans.pluginInterface.Lookup;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.regex.Pattern;

public class CommandHandler {
	private BukkitInterface MCBans;
	private Settings Config;
	private static final Pattern pattern = Pattern.compile("^\\w{2,16}$");
	//private String[] protectedGroups;
	private HashMap<String, Integer> commandList = new HashMap<String, Integer>();
	public CommandHandler(Settings cf, BukkitInterface p){
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
		Lookup lookupControl = null;
		String CommandSend = "";
		String PlayerIP = "";
		Kick kickControl = null;
		boolean commandSet = false;
		boolean isPlayer = false;
        Player playerStuff = null;
		String inWorld = "";
		String reasonString = "";
		Ban banControl = null;
		if (from instanceof Player) {
            Player player = (Player) from;
            CommandSend = player.getName();
            isPlayer = true;
            inWorld = player.getWorld().getName();
            playerStuff = player;
        } else {
            CommandSend = "Console";
            isPlayer = false;
        }
		boolean useFlags 	= false;
		boolean globalBan 	= false;
		boolean tempBan 	= false;
		boolean rollback	= false;
		boolean flagsErr	= false;
		String username		= null;
		if(args.length>=1){
			if (commandList.get(command.toLowerCase()) == 0) {
				for(int i=0;i<args[0].length();i++) {
					char c = args[0].charAt(i);
					switch(c){
						case '-':
							if (i == 0) {
								useFlags = true;
							} else {
								MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Error Banning: Misplaced flag (-)" );
								return true;
							}
							break;
						case 'g':
							if (tempBan) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Error Banning: Conflicting flags, g and t" );
								return true;
							}
							globalBan = true;
							break;
						case 'r':
							if (rollback) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Error Banning: Flag already used (r)" );
								return true;
							}
							rollback = true;
                            break;
						case 't':
							if (globalBan) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Error Banning: Conflicting flags, t and g" );
								return true;
							}
							tempBan = true;
							break;
						default:
							flagsErr = true;
							break;
					}	
					if (!useFlags) {
						break;
					}
					if (flagsErr) {
						MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Error Banning: Invalid flag!" );
						return true;
					}
				}
                if (useFlags) {
				    // /ban [0]-g [1]<player> [2]<reason>
				    if(args.length<=1){
				    	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
				    	return true;
				    }
                    if (rollback && MCBans.lbconsumer == null) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "LogBlock was not found - You cannot use flag 'r'" );
				    	return true;
                    }
				    Player target = MCBans.getServer().getPlayer(args[1]);
				    username = args[1];
				    if( target!=null ){
				    	PlayerIP = target.getAddress().getAddress().getHostAddress();
				    }
                }
			}
			if (!useFlags) {
				Player target = MCBans.getServer().getPlayer(args[0]);
				username = args[0];
				if (!pattern.matcher(username).matches()) {
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Invalid player name - Partial usernames cannot be used for bans" );
					return true;
				}
				if( target!=null ){
					PlayerIP = target.getAddress().getAddress().getHostAddress();
				}
			}
		}
		switch(commandList.get(command.toLowerCase())){
			case 0:
				// Check if Global or Local
				if (useFlags) {
					if(args.length>=2){
						if (globalBan) {
							reasonString = getReason(args,"",2);
							// Check Permissions
							if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.global") || !isPlayer){
								if (reasonString == "" || reasonString == null) {
									MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
									return true;
								}
								if (rollback) {
									LogBlock logblock = (LogBlock) MCBans.getServer().getPluginManager().getPlugin("LogBlock");
									QueryParams params = new QueryParams(logblock);
									params.setPlayer(username);
									params.world = MCBans.getServer().getWorlds().get(0);
									params.silent = true;
									try {
										logblock.getCommandsHandler().new CommandRollback((CommandSender) logblock, params, true);
										MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Rollback successful!");
									} catch (Exception e) {
										MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Unable to rollback player!");
									}
								}
								banControl = new Ban( MCBans, "globalBan", username, PlayerIP, CommandSend, reasonString, "", "" );
								banControl.start();
							}else{
								MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
								MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
							}
						} else if (tempBan) {
							if(args.length<4){
					            MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					            return true;
				            }
                            // /ban -t <name> <length> <ext> <reason>
				            if(args.length==4){
					            reasonString = Config.getString("defaultTemp");
				            }else{
					            reasonString = getReason(args,"",4);
				            }
				            // Check Permissions
				            if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.temp")){
                                if (rollback) {
									LogBlock logblock = (LogBlock) MCBans.getServer().getPluginManager().getPlugin("LogBlock");
									QueryParams params = new QueryParams(logblock);
									params.setPlayer(username);
									params.world = MCBans.getServer().getWorlds().get(0);
									params.silent = true;
									try {
										logblock.getCommandsHandler().new CommandRollback((CommandSender) logblock, params, true);
										MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Rollback successful!");
									} catch (Exception e) {
										MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Unable to rollback player!");
									}
								}
					            banControl = new Ban( MCBans, "tempBan", args[1], PlayerIP, CommandSend, reasonString, args[2], args[3] );
					            banControl.start();
				            }else{
				            	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
				            	MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
				            }
						} else {
							reasonString = getReason(args,"",2);
							if (reasonString == "") {
								reasonString = Config.getString("defaultLocal");
							}
							// Check Permissions
							if(MCBans.Permissions.isAllow( inWorld, CommandSend, "ban.local") || !isPlayer){
                                if (rollback) {
									LogBlock logblock = (LogBlock) MCBans.getServer().getPluginManager().getPlugin("LogBlock");
									QueryParams params = new QueryParams(logblock);
									params.setPlayer(username);
									params.world = MCBans.getServer().getWorlds().get(0);
									params.silent = true;
									try {
										logblock.getCommandsHandler().new CommandRollback((CommandSender) logblock, params, true);
										MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Rollback successful!");
									} catch (Exception e) {
										MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Unable to rollback player!");
									}
								}
								banControl = new Ban( MCBans, "localBan", username, PlayerIP, CommandSend, reasonString, "", "" );
								banControl.start();
							}else{
								MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
								MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
							}
						}
					} else {
						MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
						return true;
					}
				} else {
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
								banControl = new Ban( MCBans, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
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
								banControl = new Ban( MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
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
							banControl = new Ban( MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
							banControl.start();
						}else{
							MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
							MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
						}
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
					banControl = new Ban( MCBans, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[1], args[2] );
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
					banControl = new Ban( MCBans, "unBan", args[0], "", CommandSend, "", "", "" );
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
					kickControl = new Kick( Config, MCBans, args[0], CommandSend, getReason(args,"",1) );
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
					lookupControl = new Lookup( MCBans, args[0], CommandSend );
					lookupControl.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log.write( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case 5:
				if(args.length==0){
					MCBans.broadcastPlayer( CommandSend, ChatColor.BLUE + "MCBans Help");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans banning" + ChatColor.BLUE + " Help with banning/unban command");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans core" + ChatColor.BLUE + " Help with core commands");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans user" + ChatColor.BLUE + " Help with user management commands");
				} else if(args.length > 1){
					if(args.length == 2) {
						if(args[0].equalsIgnoreCase("reload")) {
							if(args[1].equalsIgnoreCase("settings")) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.AQUA + "Reloading Settings..");
								Integer reload = MCBans.Settings.reload();
								if (reload == -2) {
									MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Reload failed - File missing!");
								} else if (reload == -1) {
									MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Reload failed - File integrity failed!");
								} else {
									MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Reload completed!");
								}
								return true;
							} else if(args[1].equalsIgnoreCase("language")){
								MCBans.broadcastPlayer( CommandSend, ChatColor.AQUA + "Reloading Language File..");
								Boolean reload = MCBans.Language.reload();
								if (!reload) {
									MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Reload failed - File missing!");
								} else {
									MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Reload completed!");
								}
								return true;
							}
						}
					}
				} else {
                    if(args[0].equalsIgnoreCase("banning")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/ban <name> g <reason>" + ChatColor.BLUE + " Global ban user");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/tempban <name> <time> <m(minute) or h(hour) or d(day), w(week)> <reason>" + ChatColor.BLUE + " Temp ban user");
					    MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "WARNING " + ChatColor.WHITE + "The above commands is deprecated and will be removed");
					    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/ban <name> <reason>" + ChatColor.BLUE + " Local ban user");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/ban -<flags> <name> [<reason>|<time> <m(minute) or h(hour) or d(day), w(week)> <reason>]" + ChatColor.BLUE + " New ban command");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "See /mcbans flags for flag information");
                    }else if(args[0].equalsIgnoreCase("core")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans online" + ChatColor.BLUE + " Enable callbacks to MCBans");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans offline" + ChatColor.BLUE + " Disable callbacks to MCBans");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans status" + ChatColor.BLUE + " Check the MCBans API status");
                    }else if(args[0].equalsIgnoreCase("user")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/lookup <name>" + ChatColor.BLUE + " Lookup the reputation information");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/kick <name> <reason>" + ChatColor.BLUE + " Kick user from the server");
                    }else if(args[0].equalsIgnoreCase("flags")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.BLUE + "Prefix the first flag with a minus (-)");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "g" + ChatColor.BLUE + " Global ban user");
					    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "t" + ChatColor.BLUE + " Temporarily ban user");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "r" + ChatColor.BLUE + " Rollback user via LogBlock");
                    } else if (args[0].equalsIgnoreCase("online") || args[0].equalsIgnoreCase("offline") || args[0].equalsIgnoreCase("status")) {
                        if(MCBans.Permissions.isAllow( inWorld, CommandSend, "mode") || !isPlayer){
                            if(args[0].equalsIgnoreCase("online")){
                                MCBans.broadcastPlayer( CommandSend, ChatColor.LIGHT_PURPLE + "Running online mode!" );
                                MCBans.callbackThread.goRequest();
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
                                    MCBans.callbackThread.goRequest();
                                }
                                return true;
                            }
                        }else{
                            MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
                            return true;
                        }
                    } else {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
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