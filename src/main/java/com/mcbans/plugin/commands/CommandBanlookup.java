package com.mcbans.plugin.commands;

import com.mcbans.plugin.request.BanLookupRequest;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.callBacks.BanLookupCallback;
import com.mcbans.plugin.permission.Perms;
import com.mcbans.plugin.util.Util;

import static com.mcbans.plugin.I18n.localize;

public class CommandBanlookup extends BaseCommand{
    public CommandBanlookup(){
        bePlayer = false;
        name = "banlookup";
        argLength = 1;
        usage = "lookup a player's ban history";
        banning = false;
    }

    @Override
    public void execute() {
    	target = args.remove(0); // remove target

        // check valid banID
        if (!Util.isInteger(target) || Integer.parseInt(target) < 0){
            Util.message(sender, localize("formatError"));
            return;
        }

        // Start
        BanLookupRequest request = new BanLookupRequest(plugin, new BanLookupCallback(plugin, sender), Integer.parseInt(target));
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.LOOKUP_BAN.has(sender);
    }
}