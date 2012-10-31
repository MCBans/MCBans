package com.mcbans.firestar.mcbans.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;

public class MCBansCommandHandler implements TabExecutor{
    private final BukkitInterface plugin;
    private final Settings config;

    // command map
    private Map<String, BaseCommand> commands = new HashMap<String, BaseCommand>();

    /**
     * Constructor
     */
    public MCBansCommandHandler(final BukkitInterface plugin, final Settings config){
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String[] args) {
        final String commandName = command.getName().toLowerCase(Locale.ENGLISH);
        final BaseCommand cmd = commands.get(commandName);
        if (cmd == null){
            plugin.broadcastPlayer(sender, ChatColor.RED + "This command not loaded properly!");
            return true;
        }

        // Run the command
        cmd.run(plugin, sender, commandLabel, args);

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, Command command, String commandLabel, String[] args) {
        final String commandName = command.getName().toLowerCase(Locale.ENGLISH);
        final BaseCommand cmd = commands.get(commandName);
        if (cmd == null){
            return null;
        }

        // check permission here
        if (sender != null && !cmd.permission(sender)){
            return null;
        }

        // Get tab complete list
        return cmd.tabComplete(plugin, sender, commandLabel, args);
    }

    public void registerCommand(BaseCommand bc){
        if (bc.name != null){
            commands.put(bc.name, bc);
        }else{
            plugin.log(LogLevels.WARNING, "Invalid command not registered! " + bc.getClass().getName());
        }
    }

    /* ***************************** */

    boolean handleGlobal(String command, String[] args, String CommandSend, boolean isPlayer, String PlayerIP, int reasonOffset, int minVars,
            boolean setRollback, int setRollbackTime, String reason) {
        if (!isPlayer || Perms.BAN_GLOBAL.has(CommandSend)) {
            if (args.length < minVars) {
                plugin.broadcastPlayer(CommandSend, ChatColor.DARK_RED + plugin.Language.getFormat("formatError"));
                return true;
            }
            String reasonString = reason;
            Ban banControl = null;
            if (setRollback) {
                banControl = new Ban(plugin, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), setRollback);
            } else if (setRollbackTime != 0) {
                banControl = new Ban(plugin, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), setRollbackTime);
            } else {
                banControl = new Ban(plugin, "globalBan", args[0], PlayerIP, CommandSend, reasonString, "", "");
            }
            Thread triggerThread = new Thread(banControl);
            triggerThread.start();
        } else {
            plugin.broadcastPlayer(CommandSend, plugin.Language.getFormat("permissionDenied"));
            plugin.log(CommandSend + " has tried the command [" + command + "]!");
        }
        return true;
    }

    boolean handleTemp(String command, String[] args, String CommandSend, boolean isPlayer, String PlayerIP, int reasonOffset, int minVars,
            boolean setRollback, int setRollbackTime, int tempBanDuration, int tempBanMeasure, String reason) {
        if (!isPlayer || Perms.BAN_TEMP.has(CommandSend)) {
            if (args.length < minVars) {
                plugin.broadcastPlayer(CommandSend, ChatColor.DARK_RED + plugin.Language.getFormat("formatError"));
                return true;
            }
            String reasonString = "";
            if (args.length == minVars) {
                reasonString = config.getString("defaultTemp");
            } else {
                reasonString = reason;
            }
            Ban banControl = null;
            if (setRollback) {
                banControl = new Ban(plugin, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[tempBanDuration], args[tempBanMeasure],
                        (new JSONObject()), setRollback);
            } else if (setRollbackTime != 0) {
                banControl = new Ban(plugin, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[tempBanDuration], args[tempBanMeasure],
                        (new JSONObject()), setRollbackTime);
            } else {
                banControl = new Ban(plugin, "tempBan", args[0], PlayerIP, CommandSend, reasonString, args[tempBanDuration], args[tempBanMeasure]);
            }
            Thread triggerThread = new Thread(banControl);
            triggerThread.start();
        } else {
            plugin.broadcastPlayer(CommandSend, ChatColor.DARK_RED + plugin.Language.getFormat("permissionDenied"));
            plugin.log(CommandSend + " has tried the command [" + command + "]!");
        }
        return true;
    }

    boolean handleLocal(String command, String[] args, String CommandSend, boolean isPlayer, String PlayerIP, int reasonOffset, int minVars,
            boolean setRollback, int setRollbackTime, String reason) {
        if (!isPlayer || Perms.BAN_LOCAL.has(CommandSend)) {
            if (args.length < minVars) {
                plugin.broadcastPlayer(CommandSend, ChatColor.DARK_RED + plugin.Language.getFormat("formatError"));
                return true;
            }
            String reasonString = "";
            if (args.length == minVars) {
                reasonString = config.getString("defaultLocal");
            } else {
                reasonString = reason;
            }
            Ban banControl = null;
            if (setRollback) {
                banControl = new Ban(plugin, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), true);
            } else if (setRollbackTime != 0) {
                banControl = new Ban(plugin, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "", (new JSONObject()), setRollbackTime);
            } else {
                banControl = new Ban(plugin, "localBan", args[0], PlayerIP, CommandSend, reasonString, "", "");
            }
            Thread triggerThread = new Thread(banControl);
            triggerThread.start();
        } else {
            plugin.broadcastPlayer(CommandSend, plugin.Language.getFormat("permissionDenied"));
            plugin.log(CommandSend + " has tried the command [" + command + "]!");
        }
        return true;
    }
}
