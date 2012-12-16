package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.callBacks.LookupCallback;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.LookupRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandLookup extends BaseCommand{
    public CommandLookup(){
        bePlayer = false;
        name = "lookup";
        argLength = 1;
        usage = "lookup player";
        banning = false;
    }

    @Override
    public void execute() {
        final String target = args.get(0).trim();

        // check isValid player name
        if (!Util.isValidName(target)){
            Util.message(sender, ChatColor.RED + _("invalidName"));
            return;
        }

        // Start
        LookupRequest request = new LookupRequest(plugin, new LookupCallback(plugin, sender), target, sender.getName());
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.LOOKUP_PLAYER.has(sender);
    }
}
