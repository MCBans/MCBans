package com.mcbans.firestar.mcbans.commands;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.callBacks.ManualResync;
import com.mcbans.firestar.mcbans.callBacks.ManualSync;
import com.mcbans.firestar.mcbans.callBacks.Ping;
import com.mcbans.firestar.mcbans.callBacks.serverChoose;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;
import com.mcbans.firestar.mcbans.pluginInterface.Kick;
import com.mcbans.firestar.mcbans.pluginInterface.Lookup;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
    private BukkitInterface MCBans;
    private Settings Config;

    public CommandHandler(Settings cf, BukkitInterface p) {
        MCBans = p;
        Config = cf;
    }

    public boolean execCommand(String command, String[] args, CommandSender from) {
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
        if (args.length >= 1) {
            Player target = MCBans.getServer().getPlayer(args[0]);
            if (target != null) {
                PlayerIP = target.getAddress().getAddress().getHostAddress();
            }
        }
        switch (Commands.valueOf(command.toUpperCase())) {
        case GBAN:
            if (args.length >= 2) {
                return handleGlobal(command, args, CommandSend, isPlayer, PlayerIP, 1, 2, false, 0);
            }
            break;
        case BAN:
            // Check if Global or Local
            if (args.length < 1) {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                return true;
            }
            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("g")) {
                    return handleGlobal(command, args, CommandSend, isPlayer, PlayerIP, 2, 3, false, 0);
                } else if (args[1].equalsIgnoreCase("t")) {
                    return handleTemp(command, args, CommandSend, isPlayer, PlayerIP, 4, 4, false, 0, 2, 3);
                }
            }
            if (args.length >= 1) {
                return handleLocal(command, args, CommandSend, isPlayer, PlayerIP, 1, 1, false, 0);
            }
            break;
        case RBAN:
            // Check if Global or Local
            if (MCBans.Permissions.isAllow(CommandSend, "ban.rollback") || !isPlayer) {
                if (args.length < 1) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                    return true;
                }
                if (args.length > 1) {
                    if (isNum(args[1])) {
                        if (args.length >= 4) {
                            if (args[2].equalsIgnoreCase("g")) {
                                return handleGlobal(command, args, CommandSend, isPlayer, PlayerIP, 3, 4, false, Integer.valueOf(args[1]));
                            } else if (args[2].equalsIgnoreCase("t")) {
                                return handleTemp(command, args, CommandSend, isPlayer, PlayerIP, 5, 5, false, Integer.valueOf(args[1]), 3, 4);
                            }
                        }
                        return handleLocal(command, args, CommandSend, isPlayer, PlayerIP, 2, 2, false, Integer.valueOf(args[1]));
                    } else {
                        if (args.length >= 3) {
                            if (args[1].equalsIgnoreCase("g")) {
                                return handleGlobal(command, args, CommandSend, isPlayer, PlayerIP, 2, 3, true, 0);
                            } else if (args[1].equalsIgnoreCase("t")) {
                                return handleTemp(command, args, CommandSend, isPlayer, PlayerIP, 4, 4, true, 0, 2, 3);
                            }
                        }
                    }

                }
                if (args.length >= 1) {
                    return handleLocal(command, args, CommandSend, isPlayer, PlayerIP, 1, 1, true, 0);
                }
            } else {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                MCBans.log(CommandSend + " has tried the command [" + command + "]!");
            }
            break;
        case TBAN:
        case TEMPBAN:
            if (args.length >= 3) {
                return handleTemp(command, args, CommandSend, isPlayer, PlayerIP, 3, 3, false, 0, 1, 2);
            }
            break;
        case UNBAN:
            if (MCBans.Permissions.isAllow(CommandSend, "unban") || !isPlayer) {
                if (args.length < 1) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                    return true;
                }
                banControl = new Ban(MCBans, "unBan", args[0], "", CommandSend, "", "", "");
                Thread triggerThread = new Thread(banControl);
                triggerThread.start();
            } else {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                MCBans.log(CommandSend + " has tried the command [" + command + "]!");
            }
            commandSet = true;
            break;
        case KICK:
            if (MCBans.Permissions.isAllow(CommandSend, "kick") || !isPlayer) {
                if (args.length < 1) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                    return true;
                }
                if (args.length == 1) {
                    reasonString = Config.getString("defaultKick");
                } else {
                    reasonString = getReason(args, "", 1);
                }
                Kick kickPlayer = new Kick(MCBans.Settings, MCBans, args[0], CommandSend, reasonString);
                Thread triggerThread = new Thread(kickPlayer);
                triggerThread.start();
            } else {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                MCBans.log(CommandSend + " has tried the command [" + command + "]!");
            }
            commandSet = true;
            break;
        case LOOKUP:
        case LUP:
            if (MCBans.Permissions.isAllow(CommandSend, "lookup") || !isPlayer) {
                if (args.length < 1) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                    return true;
                }
                lookupControl = new Lookup(MCBans, args[0], CommandSend);
                Thread triggerThread = new Thread(lookupControl);
                triggerThread.start();
            } else {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                MCBans.log(CommandSend + " has tried the command [" + command + "]!");
            }
            commandSet = true;
            break;
        case MCBANS:
            if (args.length == 0) {
                MCBans.broadcastPlayer(CommandSend, ChatColor.BLUE + "MCBans Help");
                MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans banning" + ChatColor.BLUE + " Help with banning/unban command");
                MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans get" + ChatColor.BLUE + " Get time till next call");
                MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans ping" + ChatColor.BLUE + " Check overall response time from API");
                MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans reload" + ChatColor.BLUE + " Reload settings and language file");
                MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans sync" + ChatColor.BLUE + " Force a sync to occur");
                MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans user" + ChatColor.BLUE + " Help with user management commands");
            } else if (args.length > 1) {
                if (MCBans.Permissions.isAllow(CommandSend, "admin") || !isPlayer) {
                    if (args[0].equalsIgnoreCase("get")) {
                        if(args[0].equalsIgnoreCase("get")){
                            if(args[1].equalsIgnoreCase("call")){
                                long callBackInterval = 0;
                                callBackInterval = (60)*MCBans.Settings.getInteger("callBackInterval");
                                if(callBackInterval<((60)*15)){
                                    callBackInterval=((60)*15);
                                }
                                String r = this.timeRemain( (MCBans.lastCallBack+callBackInterval) - (System.currentTimeMillis()/1000) );
                                MCBans.broadcastPlayer( CommandSend, ChatColor.GOLD + r + " until next callback request."  );
                            }else if(args[1].equalsIgnoreCase("sync")){
                                long syncInterval = MCBans.Settings.getInteger("syncInterval");
                                if(syncInterval<((60)*5)){
                                    syncInterval=((60)*5);
                                }
                                String r = this.timeRemain( (MCBans.lastSync+syncInterval) - (System.currentTimeMillis()/1000) );
                                MCBans.broadcastPlayer( CommandSend, ChatColor.GOLD + r + " until next sync." );
                            }
                        }else if(args[0].equalsIgnoreCase("sync")){
                            if(args[1].equalsIgnoreCase("all")){
                                MCBans.broadcastPlayer(CommandSend, ChatColor.GREEN + " Re-Sync has started!");
                                ManualResync manualSyncBanRunner = new ManualResync( MCBans, CommandSend );
                                (new Thread(manualSyncBanRunner)).start();
                                return true;
                            }
                        }
                    }
                } else {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                    MCBans.log(CommandSend + " has tried the command [" + command + "]!");
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("banning")) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/ban <name> <reason>" + ChatColor.BLUE + " Local ban user");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/ban <name> g <reason>" + ChatColor.BLUE + " Global ban user");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/ban <name> t <time> <m or h or d> <reason>" + ChatColor.BLUE
                            + " Temporarily ban");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE
                            + "/tban <name> <time> <m(minute) or h(hour) or d(day), w(week)> <reason>" + ChatColor.BLUE + " Temp ban user");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/gban <name> <reason>" + ChatColor.BLUE + " Global ban user");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/rban <name> <reason>" + ChatColor.BLUE + " Rollback and local ban");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/rban <name> g <reason>" + ChatColor.BLUE + " Rollback and global ban");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/rban <name> t <time> <m or h or d> <reason>" + ChatColor.BLUE
                            + " Rollback and temporarily ban");
                } else if (args[0].equalsIgnoreCase("user")) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/lookup <name>" + ChatColor.BLUE
                            + " Lookup the reputation information");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/kick <name> <reason>" + ChatColor.BLUE + " Kick user from the server");
                } else if (args[0].equalsIgnoreCase("ping")) {
                    if (MCBans.Permissions.isAllow(CommandSend, "admin") || !isPlayer) {
                        Ping manualPingCheck = new Ping(MCBans, CommandSend);
                        (new Thread(manualPingCheck)).start();
                    } else {
                        MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                        MCBans.log(CommandSend + " has tried the command [" + command + "]!");
                    }
                } else if (args[0].equalsIgnoreCase("sync")) {
                    if (MCBans.Permissions.isAllow(CommandSend, "admin") || !isPlayer) {
                        long syncInterval = MCBans.Settings.getInteger("syncInterval");
                        if(syncInterval<((60)*5)){
                            syncInterval=((60)*5);
                        }
                        long ht = (MCBans.lastSync+syncInterval) - (System.currentTimeMillis()/1000);
                        if (ht > 10) {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.GREEN + " Sync has started!");
                            ManualSync manualSyncBanRunner = new ManualSync(MCBans, CommandSend);
                            (new Thread(manualSyncBanRunner)).start();
                        } else {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.RED + "[Unable] Sync will occur in less than 10 seconds!");
                        }
                    } else {
                        MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                        MCBans.log(CommandSend + " has tried the command [" + command + "]!");
                    }
                } else if (args[0].equalsIgnoreCase("get")) {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans get call" + ChatColor.BLUE
                            + " Time until callback thread sends data.");
                    MCBans.broadcastPlayer(CommandSend, ChatColor.WHITE + "/mcbans get sync" + ChatColor.BLUE + " Time until next sync.");
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (MCBans.Permissions.isAllow(CommandSend, "admin") || !isPlayer) {
                        MCBans.broadcastPlayer(CommandSend, ChatColor.AQUA + "Reloading Settings..");
                        Integer reloadSettings = MCBans.Settings.reload();
                        if (reloadSettings == -2) {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.RED + "Reload failed - File missing!");
                        } else if (reloadSettings == -1) {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.RED + "Reload failed - File integrity failed!");
                        } else {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.GREEN + "Reload completed!");
                        }
                        MCBans.broadcastPlayer(CommandSend, ChatColor.AQUA + "Reloading Language File..");
                        boolean reloadLanguage = MCBans.Language.reload();
                        if (!reloadLanguage) {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.RED + "Reload failed - File missing!");
                        } else {
                            MCBans.broadcastPlayer(CommandSend, ChatColor.GREEN + "Reload completed!");
                        }
                        serverChoose serverChooser = new serverChoose(MCBans);
                        (new Thread(serverChooser)).start();
                    } else {
                        MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
                        MCBans.log(CommandSend + " has tried the command [" + command + "]!");
                    }
                } else {
                    MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                }
            } else {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
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

    private boolean isNum(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private String timeRemain(long remain) {
        try {
            String format = "";
            long timeRemaining = remain;
            long sec = timeRemaining % 60;
            long min = (timeRemaining / 60) % 60;
            long hours = (timeRemaining / (60 * 60)) % 24;
            long days = (timeRemaining / (60 * 60 * 24)) % 7;
            long weeks = (timeRemaining / (60 * 60 * 24 * 7));
            if (sec != 0) {
                format = sec + " seconds";
            }
            if (min != 0) {
                format = min + " minutes " + format;
            }
            if (hours != 0) {
                format = hours + " hours " + format;
            }
            if (days != 0) {
                format = days + " days " + format;
            }
            if (weeks != 0) {
                format = weeks + " weeks " + format;
            }
            return format;
        } catch (ArithmeticException e) {
            return "";
        }
    }

    private boolean handleGlobal(String command, String[] args, String CommandSend, boolean isPlayer, String PlayerIP, int reasonOffset, int minVars,
            boolean setRollback, int setRollbackTime) {
        if (MCBans.Permissions.isAllow(CommandSend, "ban.global") || !isPlayer) {
            if (args.length < minVars) {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                return true;
            }
            String reasonString = getReason(args, "", reasonOffset);
            Ban banControl = null;
            if (setRollback) {
                banControl = new Ban(MCBans, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), setRollback);
            } else if (setRollbackTime != 0) {
                banControl = new Ban(MCBans, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), setRollbackTime);
            } else {
                banControl = new Ban(MCBans, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "");
            }
            Thread triggerThread = new Thread(banControl);
            triggerThread.start();
        } else {
            MCBans.broadcastPlayer(CommandSend, MCBans.Language.getFormat("permissionDenied"));
            MCBans.log(CommandSend + " has tried the command [" + command + "]!");
        }
        return true;
    }

    public boolean handleTemp(String command, String[] args, String CommandSend, boolean isPlayer, String PlayerIP, int reasonOffset, int minVars,
            boolean setRollback, int setRollbackTime, int tempBanDuration, int tempBanMeasure) {
        if (MCBans.Permissions.isAllow(CommandSend, "ban.temp") || !isPlayer) {
            if (args.length < minVars) {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                return true;
            }
            String reasonString = "";
            if (args.length == minVars) {
                reasonString = Config.getString("defaultTemp");
            } else {
                reasonString = getReason(args, "", reasonOffset);
            }
            Ban banControl = null;
            if (setRollback) {
                banControl = new Ban(MCBans, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[tempBanDuration], args[tempBanMeasure],
                        (new JSONObject()), setRollback);
            } else if (setRollbackTime != 0) {
                banControl = new Ban(MCBans, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[tempBanDuration], args[tempBanMeasure],
                        (new JSONObject()), setRollbackTime);
            } else {
                banControl = new Ban(MCBans, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[tempBanDuration], args[tempBanMeasure]);
            }
            Thread triggerThread = new Thread(banControl);
            triggerThread.start();
        } else {
            MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("permissionDenied"));
            MCBans.log(CommandSend + " has tried the command [" + command + "]!");
        }
        return true;
    }

    public boolean handleLocal(String command, String[] args, String CommandSend, boolean isPlayer, String PlayerIP, int reasonOffset, int minVars,
            boolean setRollback, int setRollbackTime) {
        if (MCBans.Permissions.isAllow(CommandSend, "ban.local") || !isPlayer) {
            if (args.length < minVars) {
                MCBans.broadcastPlayer(CommandSend, ChatColor.DARK_RED + MCBans.Language.getFormat("formatError"));
                return true;
            }
            String reasonString = "";
            if (args.length == minVars) {
                reasonString = Config.getString("defaultLocal");
            } else {
                reasonString = getReason(args, "", reasonOffset);
            }
            Ban banControl = null;
            if (setRollback) {
                banControl = new Ban(MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), true);
            } else if (setRollbackTime != 0) {
                banControl = new Ban(MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), setRollbackTime);
            } else {
                banControl = new Ban(MCBans, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "");
            }
            Thread triggerThread = new Thread(banControl);
            triggerThread.start();
        } else {
            MCBans.broadcastPlayer(CommandSend, MCBans.Language.getFormat("permissionDenied"));
            MCBans.log(CommandSend + " has tried the command [" + command + "]!");
        }
        return true;
    }
}