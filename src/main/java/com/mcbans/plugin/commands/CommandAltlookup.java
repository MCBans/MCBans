package com.mcbans.plugin.commands;

import com.mcbans.plugin.request.AltLookupRequest;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.callBacks.AltLookupCallback;
import com.mcbans.plugin.permission.Perms;

public class CommandAltlookup extends BaseCommand{
    public CommandAltlookup(){
        bePlayer = false;
        name = "altlookup";
        argLength = 1;
        usage = "lookup a player's alternate accounts";
        banning = true;
    }

    @Override
    public void execute() {
    	args.remove(0); // remove target

        // check isValid player name
        /*if (!Util.isValidName(target)){
            Util.message(sender, ChatColor.RED + localize("invalidName"));
            return;
        }*/

        // Start
        AltLookupRequest request = new AltLookupRequest(plugin, new AltLookupCallback(plugin, sender), target);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.LOOKUP_ALT.has(sender);
    }
}
