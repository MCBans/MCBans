package com.mcbans.firestar.mcbans.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.BukkitInterface;

public abstract class BaseCommand {
    // Set this class
    public BukkitInterface plugin;
    public String command;
    public List<String> args = new ArrayList<String>();
    public CommandSender sender;
    public Player player;
    public boolean isPlayer = false;

    // Set extend class constructor
    public String name;
    public int argLength = 0;
    public String usage;
    public boolean bePlayer = false;

    public boolean run(BukkitInterface plugin, CommandSender sender, String cmd, String[] preArgs) {
        if (name == null){
            plugin.broadcastPlayer(sender, "&cThis command not loaded properly!");
            return true;
        }

        this.plugin = plugin;
        this.sender = sender;
        this.command = cmd;

        // Sort args
        args.clear();
        for (String arg : preArgs)
            args.add(arg);

        // Check args size
        if (argLength > args.size()){
            sendUsage();
            return true;
        }

        // Check sender is player
        if (bePlayer && !(sender instanceof Player)){
            plugin.broadcastPlayer(sender, "&cThis command cannot run from Console!");
            return true;
        }
        if (sender instanceof Player){
            player = (Player)sender;
            isPlayer = true;
        }

        // Check permission
        if (!permission(sender)){
            plugin.broadcastPlayer(sender, "&cYou don't have permission to use this!");
            return true;
        }

        // Exec
        try {
            execute();
        }
        catch (Exception ex) {
            Throwable error = ex;
            /*
            TODO: change this
            while (error instanceof Exception){
                Actions.message(sender, null, error.getMessage());
                error = error.getCause();
            }
            */
        }

        return true;
    }

    /**
     * Execute command
     */
    public abstract void execute();

    /**
     * TabComplete
     */
    protected List<String> tabComplete(BukkitInterface plugin, final CommandSender sender, String cmd, String[] preArgs){
        return null;
    }

    /**
     * Check sender has command permission
     * @return true if sender has permission
     */
    public abstract boolean permission(CommandSender sender);

    /**
     * Send command usage
     */
    public void sendUsage(){
        // TODO: change this
        //plugin.broadcastPlayer(sender, "&c/"+this.command+" "+name+" "+usage);
    }
}
