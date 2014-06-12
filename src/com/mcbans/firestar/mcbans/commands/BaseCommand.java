package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.ConfigurationManager;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.util.Util;

public abstract class BaseCommand {
    // Set this class
    protected MCBans plugin;
    protected ConfigurationManager config;
    protected CommandSender sender;
    protected String command;

    // Needs init
    protected List<String> args = new ArrayList<String>();
    protected String senderName, senderUUID;
    protected Player player;
    protected boolean isPlayer;

    // Set this class if banning (needs init)
    protected String target = "";
    protected String targetIP = "";
    protected String targetUUID = "";

    // Set extend class constructor (Command property)
    protected String name;
    protected int argLength = 0;
    protected String usage;
    protected boolean bePlayer = false;
    protected boolean banning = false;

    public boolean run(final MCBans plugin, final CommandSender sender, final String cmd, final String[] preArgs) {
        if (name == null){
            Util.message(sender, "&cThis command not loaded properly!");
            return true;
        }

        // init command
        init();

        this.plugin = plugin;
        this.config = plugin.getConfigs();
        this.sender = sender;
        this.command = cmd;

        // Sort args

        for (String arg : preArgs)
            args.add(arg);

        // Check args size
        if (argLength > args.size()){
            //sendUsage();
            Util.message(sender, ChatColor.RED + _("formatError"));
            return true;
        }

        // Check sender is player
        if (bePlayer && !(sender instanceof Player)){
            Util.message(sender, "&cThis command cannot run from Console!");
            return true;
        }
        if (sender instanceof Player){
            player = (Player)sender;
            senderName = player.getName();
            //senderUUID = player.getUniqueId().toString();
            isPlayer = true;
        }

        // Check permission
        if (!permission(sender)){
            Util.message(sender, ChatColor.RED + _("permissionDenied"));
            //plugin.log(senderName + " has tried the command [" + command + "]!"); // maybe not needs command logger. Craftbukkit added this.
            //plugin.broadcastPlayer(sender, "&cYou don't have permission to use this!");
            return true;
        }

        // set banning information
        if (banning && args.size() > 0){
            // target = args.remove(0); // Don't touch args here
            target = args.get(0).trim();
            // get targetIP if available
            final Player targetPlayer = Bukkit.getPlayerExact(target);
            if (targetPlayer != null && targetPlayer.isOnline()){
                targetIP = targetPlayer.getAddress().getAddress().getHostAddress();
                //targetUUID = targetPlayer.getUniqueId().toString();
            }
            // check isValid player name
            if (!Util.isValidName(target)){
            	if(Util.isValidUUID(target)){
            		targetUUID = target;
            		target = "";
            	}else{
            		if(Util.isValidIP(target)){
            			targetIP = target;
            		}else{
            			Util.message(sender, ChatColor.RED + _("invalidName"));
            			return true;
            		}
            	}
            }
            System.out.println("target: "+target);
            System.out.println("targetUUID: "+targetUUID);
        }

        // Exec
        try {
            execute();
        }
        catch (CommandException ex) {
            Throwable error = ex;
            while (error instanceof Exception){
                Util.message(sender, error.getMessage());
                error = error.getCause();
            }
        }

        return true;
    }

    /**
     * Initialize command
     */
    private void init(){
        this.args.clear();
        this.player = null;
        this.isPlayer = false;
        this.senderName = "Console";

        this.target = "";
        this.targetUUID = "";
        this.targetIP = "";
    }

    /**
     * Execute command
     */
    public abstract void execute() throws CommandException;

    /**
     * TabComplete
     */
    protected List<String> tabComplete(final MCBans plugin, final CommandSender sender, final String cmd, final String[] preArgs){
        return null;
    }

    /**
     * Check sender has command permission
     * @return true if sender has permission
     */
    public abstract boolean permission(final CommandSender sender);

    /**
     * Send command usage
     */
    public void sendUsage(){
        // TODO: change this
        //plugin.broadcastPlayer(sender, "&c/"+this.command+" "+name+" "+usage);
    }
}
