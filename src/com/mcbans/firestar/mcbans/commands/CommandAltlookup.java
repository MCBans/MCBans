package com.mcbans.firestar.mcbans.commands;

import com.mcbans.firestar.mcbans.callBacks.AltLookupCallback;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.AltLookupRequest;
import org.bukkit.command.CommandSender;

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
