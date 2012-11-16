package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.pluginInterface.Lookup;
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
            Util.message(sender, _("invalidName"));
            return;
        }

        // Start
        Lookup lookupControl = new Lookup(plugin, target, senderName);
        Thread triggerThread = new Thread(lookupControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.LOOKUP.has(sender);
    }
}
