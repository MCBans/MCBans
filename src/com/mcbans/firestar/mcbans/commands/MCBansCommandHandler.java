package com.mcbans.firestar.mcbans.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class MCBansCommandHandler implements TabExecutor{
    private final MCBans plugin;

    // command map
    private Map<String, BaseCommand> commands = new HashMap<String, BaseCommand>();

    /**
     * Constructor
     */
    public MCBansCommandHandler(final MCBans plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String[] args) {
        final String commandName = command.getName().toLowerCase(Locale.ENGLISH);
        final BaseCommand cmd = commands.get(commandName);
        if (cmd == null){
            Util.message(sender, ChatColor.RED + "This command has not been loaded properly!");
            return true;
        }
        
        if (!(cmd instanceof CommandMcbans)){
            if (!plugin.getConfigs().isValidApiKey()){
                Util.message(sender, ChatColor.RED + "Missing or Invalid API key! Check API Key or contact MCBans Staff!");
                return true;
            }
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

    public void registerCommand(final BaseCommand bc){
        if (bc.name != null){
            commands.put(bc.name, bc);
        }else{
            plugin.getLog().warning("Invalid command! " + bc.getClass().getName());
        }
    }
}
