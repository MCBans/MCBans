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
        usage = "lookup player ban history";
        banning = false;
    }

    @Override
    public void execute() {
    	target = args.remove(0); // remove target

        // check valid banID
        if (!Util.isInteger(target) || Integer.parseInt(target) < 0){
            Util.message(sender, _("formatError"));
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