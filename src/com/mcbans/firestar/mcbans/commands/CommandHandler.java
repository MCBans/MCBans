package com.mcbans.firestar.mcbans.commands;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.callBacks.ManualSync;
import com.mcbans.firestar.mcbans.callBacks.Ping;
import com.mcbans.firestar.mcbans.callBacks.serverChoose;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;
import com.mcbans.firestar.mcbans.pluginInterface.Kick;
import com.mcbans.firestar.mcbans.pluginInterface.Lookup;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class CommandHandler {
	private BukkitInterface MCBans;
	private Settings Config;
    private static final Pattern pattern = Pattern.compile("^\\w{2,16}$");

	public CommandHandler(Settings cf, BukkitInterface p){
		MCBans = p;
		Config=cf;
	}
	public boolean execCommand(String command, String[] args, CommandSender from){
		Lookup lookupControl = null;
		String CommandSend = "";
		String PlayerIP = "";
		boolean commandSet = false;
		boolean isPlayer = false;
		String reasonString = "";
		Ban banControl = null;
		if (from instanceof Player) {
            Player player = (Player) from;
            CommandSend = player.getName();
            isPlayer = true;
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
			if (Commands.valueOf(command.toUpperCase()) == Commands.BAN) {
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
				if( target!=null ){
					PlayerIP = target.getAddress().getAddress().getHostAddress();
				}
			}
		}
		switch(Commands.valueOf(command.toUpperCase())){
			case BAN:
                // Check username
                if (!pattern.matcher(username).matches()) {
                    MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + "Invalid player name - Partial usernames cannot be used" );
                    return true;
                }
                // Check if Global or Local
				if (useFlags) {
					if(args.length>=2){
						if (globalBan) {
							reasonString = getReason(args,"",2);
							// Check Permissions
							if(MCBans.Permissions.isAllow(  CommandSend, "ban.global") || !isPlayer){
								if (reasonString.equals("") || reasonString == null) {
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
								Thread triggerThread = new Thread(banControl);
								triggerThread.start();
							}else{
								MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
								MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
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
				            if(MCBans.Permissions.isAllow(  CommandSend, "ban.temp")){
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
					            Thread triggerThread = new Thread(banControl);
								triggerThread.start();
				            }else{
				            	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
				            	MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
				            }
						} else {
							reasonString = getReason(args,"",2);
							if (reasonString.equals("")) {
								reasonString = Config.getString("defaultLocal");
							}
							// Check Permissions
							if(MCBans.Permissions.isAllow( CommandSend, "ban.local") || !isPlayer){
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
								Thread triggerThread = new Thread(banControl);
								triggerThread.start();
							}else{
								MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
								MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
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
                            if(MCBans.Permissions.isAllow(  CommandSend, "ban.global") || !isPlayer){
							    if(args.length<3){
							    	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
							    	return true;
							    }
							    reasonString = getReason(args,"",2);
								banControl = new Ban( MCBans, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
								Thread triggerThread = new Thread(banControl);
								triggerThread.start();
							}else{
								MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
								MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
							}
						}else{
                            if(MCBans.Permissions.isAllow(  CommandSend, "ban.local") || !isPlayer){
                                if(args.length<1){
                                    MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
							    	return true;
							    }
							    if(args.length==1){
							    	reasonString = Config.getString("defaultLocal");
							    }else{
							    	reasonString = getReason(args,"",1);
							    }
								banControl = new Ban( MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
								Thread triggerThread = new Thread(banControl);
								triggerThread.start();
							}else{
								MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
								MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
							}
						}
					}else{
                        if(MCBans.Permissions.isAllow( CommandSend, "ban.local") || !isPlayer){
						    if(args.length<1){
						    	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
						    	return true;
						    }
						    if(args.length==1){
						    	reasonString = Config.getString("defaultLocal");
						    }else{
						    	reasonString = getReason(args,"",1);
						    }
							banControl = new Ban( MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "" );
							Thread triggerThread = new Thread(banControl);
							triggerThread.start();
						}else{
							MCBans.broadcastPlayer( CommandSend, MCBans.Language.getFormat( "permissionDenied" ) );
							MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
						}
					}
				}
				commandSet = true;
			break;
			case TEMPBAN:
                if(MCBans.Permissions.isAllow(  CommandSend, "ban.temp") || !isPlayer){
				    if(args.length<3){
				    	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
				    	return true;
				    }
				    if(args.length==3){
				    	reasonString = Config.getString("defaultTemp");
				    }else{
				    	reasonString = getReason(args,"",3);
				    }
					banControl = new Ban( MCBans, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[1], args[2] );
					Thread triggerThread = new Thread(banControl);
					triggerThread.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case UNBAN:
                if(MCBans.Permissions.isAllow( CommandSend, "unban") || !isPlayer){
				    if(args.length<1){
				    	MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
				    	return true;
				    }
					banControl = new Ban( MCBans, "unBan", args[0], "", CommandSend, "", "", "" );
					Thread triggerThread = new Thread(banControl);
					triggerThread.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case KICK:
                if(MCBans.Permissions.isAllow(  CommandSend, "kick") || !isPlayer){
				    if(args.length<1){
					    MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					    return true;
				    }
				    if(args.length==1){
				    	reasonString = Config.getString("defaultKick");
				    }else{
				    	reasonString = getReason(args,"",1);
				    }
					Kick kickPlayer = new Kick( MCBans.Settings, MCBans, args[0], CommandSend, reasonString );
					Thread triggerThread = new Thread(kickPlayer);
					triggerThread.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case LOOKUP:
            case LUP:
                if(MCBans.Permissions.isAllow( CommandSend, "lookup") || !isPlayer){
				    if(args.length<1){
					    MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "formatError" ) );
					    return true;
				    }
					lookupControl = new Lookup( MCBans, args[0], CommandSend );
					Thread triggerThread = new Thread(lookupControl);
					triggerThread.start();
				}else{
					MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					MCBans.log( CommandSend + " has tried the command ["+command+"]!" );
				}
				commandSet = true;
			break;
			case MCBANS:
				if(args.length==0){
					MCBans.broadcastPlayer( CommandSend, ChatColor.BLUE + "MCBans Help");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans banning" + ChatColor.BLUE + " Help with banning/unban command");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans get" + ChatColor.BLUE + " Get time till next call");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans ping" + ChatColor.BLUE + " Check overall response time from API");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans reload" + ChatColor.BLUE + " Reload settings and language file");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans sync" + ChatColor.BLUE + " Force a sync to occur");
                    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans user" + ChatColor.BLUE + " Help with user management commands");
				} else if(args.length >= 1){
					if(MCBans.Permissions.isAllow( CommandSend, "admin") || !isPlayer){
						if(args[0].equalsIgnoreCase("reload")) {
							MCBans.broadcastPlayer( CommandSend, ChatColor.AQUA + "Reloading Settings..");
							Integer reloadSettings = MCBans.Settings.reload();
							if (reloadSettings == -2) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Reload failed - File missing!");
							} else if (reloadSettings == -1) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Reload failed - File integrity failed!");
							} else {
								MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Reload completed!");
							}
							MCBans.broadcastPlayer( CommandSend, ChatColor.AQUA + "Reloading Language File..");
							boolean reloadLanguage = MCBans.Language.reload();
							if (!reloadLanguage) {
								MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "Reload failed - File missing!");
							} else {
								MCBans.broadcastPlayer( CommandSend, ChatColor.GREEN + "Reload completed!");
							}
							serverChoose serverChooser = new serverChoose( MCBans );
					        (new Thread(serverChooser)).start();
							return true;
						}else if(args[0].equalsIgnoreCase("get")){
							if(args.length == 2){
								if(args[1].equalsIgnoreCase("call")){
									String r = this.timeRemain( (MCBans.lastCallBack+(60*MCBans.Settings.getInteger("callBackInterval"))) - (System.currentTimeMillis()/1000) );
									MCBans.broadcastPlayer( CommandSend, ChatColor.GOLD + r + " until next callback request."  );
								}else if(args[1].equalsIgnoreCase("sync")){
									String r = this.timeRemain( (MCBans.lastSync+(60*MCBans.Settings.getInteger("syncInterval"))) - (System.currentTimeMillis()/1000) );
									MCBans.broadcastPlayer( CommandSend, ChatColor.GOLD + r + " until next sync." );
								}
							}else{
								MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans get call" + ChatColor.BLUE + " Time until callback thread sends data.");
								MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/mcbans get sync" + ChatColor.BLUE + " Time until next sync.");
							}
						}else if(args[0].equalsIgnoreCase("sync")){
							long ht = (MCBans.lastSync+(60*MCBans.Settings.getInteger("syncInterval"))) - (System.currentTimeMillis()/1000);
							if(ht>20){
								MCBans.broadcastPlayer(CommandSend, ChatColor.GREEN + " Sync has started!");
								ManualSync manualSyncBanRunner = new ManualSync( MCBans, CommandSend );
								(new Thread(manualSyncBanRunner)).start();
							}else{
								MCBans.broadcastPlayer(CommandSend, ChatColor.RED + "[Unable] Sync will occur in less than 20 seconds!");
							}
						}else if(args[0].equalsIgnoreCase("ping")){
							Ping manualPingCheck = new Ping( MCBans, CommandSend );
							(new Thread(manualPingCheck)).start();
						}
					}else{
						MCBans.broadcastPlayer( CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat( "permissionDenied" ) );
					}
					return true;
				} else {
                    if(args[0].equalsIgnoreCase("banning")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/ban <name> g <reason>" + ChatColor.BLUE + " Global ban user");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/tempban <name> <time> <m(minute) or h(hour) or d(day), w(week)> <reason>" + ChatColor.BLUE + " Temp ban user");
					    MCBans.broadcastPlayer( CommandSend, ChatColor.RED + "WARNING " + ChatColor.WHITE + "The above commands is deprecated and will be removed");
					    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/ban <name> <reason>" + ChatColor.BLUE + " Local ban user");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/ban -<flags> <name> [<reason>|<time> <m(minute) or h(hour) or d(day), w(week)> <reason>]" + ChatColor.BLUE + " New ban command");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "See /mcbans flags for flag information");
                    }else if(args[0].equalsIgnoreCase("user")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/lookup <name>" + ChatColor.BLUE + " Lookup the reputation information");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "/kick <name> <reason>" + ChatColor.BLUE + " Kick user from the server");
                    }else if(args[0].equalsIgnoreCase("flags")) {
                        MCBans.broadcastPlayer( CommandSend, ChatColor.BLUE + "Prefix the first flag with a minus (-)");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "g" + ChatColor.BLUE + " Global ban user");
					    MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "t" + ChatColor.BLUE + " Temporarily ban user");
                        MCBans.broadcastPlayer( CommandSend, ChatColor.WHITE + "r" + ChatColor.BLUE + " Rollback user via LogBlock");
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
	private String timeRemain(long remain){
		try
		{
			String format = "";
			long timeRemaining = remain;
			long sec = timeRemaining % 60;
			long min = (timeRemaining/60) % 60;
			long hours = (timeRemaining/(60*60)) % 24;
			long days = (timeRemaining/(60*60*24)) % 7;
			long weeks = (timeRemaining/(60*60*24*7)); 
			if (sec != 0){
				format = sec + " seconds";
			}
			if (min != 0){
				format = min + " minutes " + format;
			}
			if (hours != 0){
				format = hours + " hours " + format;
			}
			if (days != 0){
				format = days + " days " + format;
			}
			if (weeks != 0){
				format = weeks + " weeks " + format;
			}
			return format;
		}catch(ArithmeticException e){
			return "";
		} 
	}
}