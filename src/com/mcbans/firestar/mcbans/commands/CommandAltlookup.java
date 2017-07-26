package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.callBacks.AltLookupCallback;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.AltLookupRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandAltlookup extends BaseCommand{
    public CommandAltlookup(){
        bePlayer = false;
        name = "altlookup";
        argLength = 1;
        usage = "lookup player alternate accounts";
        banning = true;
    }

    @Override
    public void execute() {
    	args.remove(0); // remove target

        // check isValid player name
        /*if (!Util.isValidName(target)){
            Util.message(sender, ChatColor.RED + _("invalidName"));
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
