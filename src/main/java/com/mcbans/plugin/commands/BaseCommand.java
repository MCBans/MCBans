package com.mcbans.plugin.commands;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mcbans.utils.IPTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.plugin.ConfigurationManager;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.exception.CommandException;
import com.mcbans.plugin.util.Util;

import static com.mcbans.plugin.I18n.localize;

public abstract class BaseCommand {
    // Set this class
    protected MCBans plugin;
    protected ConfigurationManager config;
    protected CommandSender sender;
    protected String command=null;

    // Needs init
    protected List<String> args = new ArrayList<String>();

    // Sender
    protected Player senderPlayer = null;
    protected String senderName = null;
    protected String senderUUID = null;

    // Set this class if banning (needs init)
    protected String target = null;
    protected String targetIP = null;
    protected String targetUUID = null;

    // Set extend class constructor (Command property)
    protected String name = null;
    protected int argLength = 0;
    protected String usage = null;
    protected boolean bePlayer = false;
    protected boolean banning = false;

    public boolean run(final MCBans plugin, final CommandSender sender, final String cmd, final String[] preArgs) {
        if (name == null){
            Util.message(sender, "&cThis command has not been loaded properly.");
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
            Util.message(sender, ChatColor.RED + localize("formatError"));
            return true;
        }

        // Check sender is player
        if (bePlayer && !(sender instanceof Player)){
            Util.message(sender, "&cThis command cannot be executed from the console.");
            return true;
        }
        if (sender instanceof Player){
            senderPlayer = (Player)sender;
            senderName = senderPlayer.getName();
            senderUUID = senderPlayer.getUniqueId().toString().replaceAll("-", "");
        }

        // Check permission
        if (!permission(sender)){
            Util.message(sender, ChatColor.RED + localize("permissionDenied"));
            //plugin.log(senderName + " has tried the command [" + command + "]!"); // maybe not needs command logger. Craftbukkit added this.
            //plugin.broadcastPlayer(sender, "&cYou don't have permission to use this!");
            return true;
        }

        // set banning information
        if (banning && args.size() > 0){
            // target = args.remove(0); // Don't touch args here
            target = args.get(0).trim();
            // get targetIP if available
            final Player targetPlayer = MCBans.getPlayer(plugin, target);
            if (targetPlayer != null && targetPlayer.isOnline()){
                InetSocketAddress socket = targetPlayer.getAddress();
                //Not all IPs are successfully resolved
                //This prevents players from being unbannable/kickable in this rare case
                if(socket.isUnresolved()) {
                    targetIP = null;
                } else {
                    targetIP = socket.getAddress().getHostAddress();
                }
            }
            // check isValid player name
            if (!Util.isValidName(target)){
            	if(Util.isValidUUID(target)){
            		targetUUID = target;
            		target = null;
            	}else{
            		if(IPTools.validBanIP(target)){
            			targetIP = target;
            		}else{
            			Util.message(sender, ChatColor.RED + localize("invalidName"));
            			return true;
            		}
            	}
            }
            //System.out.println("target: "+target);
            //System.out.println("targetUUID: "+targetUUID);
        }

        // Exec
        try {
            check();
            execute();
        } catch (CommandException ex) {
            Throwable error = ex;
            while (error instanceof Exception) {
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
        this.senderPlayer = null;
        this.senderName = "Console";

        this.target = null;
        this.targetUUID = null;
        this.targetIP = null;
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

    private void check() throws CommandException {
        /*if(banning) {
            if (sender instanceof Player) {
                if(targetUUID==null) {
                    if (!Util.checkVault((Player) sender, Bukkit.getOfflinePlayer(target))) {
                        throw new CommandException(ChatColor.RED + localize("permissionDenied"));
                    }
                } else {
                    if (!Util.checkVault((Player) sender, Bukkit.getOfflinePlayer(UUID.fromString(targetUUID)))) {
                        throw new CommandException(ChatColor.RED + localize("permissionDenied"));
                    }
                }
            }
        }*/
    }
}
