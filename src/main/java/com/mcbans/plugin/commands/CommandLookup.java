package com.mcbans.plugin.commands;


import com.mcbans.plugin.request.LookupRequest;
import org.bukkit.command.CommandSender;

import com.mcbans.plugin.callBacks.LookupCallback;
import com.mcbans.plugin.permission.Perms;

public class CommandLookup extends BaseCommand{
    public CommandLookup(){
        bePlayer = false;
        name = "lookup";
        argLength = 1;
        usage = "lookup a player's ban history";
        banning = true;
    }

    @Override
    public void execute() {
    	args.remove(0); // remove target
        
        // check isValid player name
        /*if (!Util.isValidName(target)){
        	if(Util.isValidUUID(target)){
        		targetUUID = target;
        		target = "";
        	}else{
        		Util.message(sender, ChatColor.RED + _("invalidName"));
        		return;
            }
        }*/
        
        // Start
        LookupRequest request = new LookupRequest(plugin, new LookupCallback(plugin, sender), target, targetUUID, senderName, senderUUID);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.LOOKUP_PLAYER.has(sender);
    }
}
