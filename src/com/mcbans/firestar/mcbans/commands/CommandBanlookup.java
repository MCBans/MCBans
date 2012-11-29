package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.callBacks.BanLookupCallback;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.BanLookupRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandBanlookup extends BaseCommand{
    public CommandBanlookup(){
        bePlayer = false;
        name = "banlookup";
        argLength = 1;
        usage = "lookup ban";
        banning = false;
    }

    @Override
    public void execute() {
        final String banID = args.get(0).trim();

        // check valid banID
        if (!Util.isInteger(banID) || Integer.parseInt(banID) < 0){
            Util.message(sender, _("formatError"));
            return;
        }

        // Start
        BanLookupRequest request = new BanLookupRequest(plugin, new BanLookupCallback(plugin, sender), Integer.parseInt(banID));
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.LOOKUP_BAN.has(sender);
    }
}