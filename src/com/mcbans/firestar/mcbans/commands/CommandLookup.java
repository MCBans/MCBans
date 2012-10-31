package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.pluginInterface.Lookup;

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
        String target = args.get(0).trim();

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
