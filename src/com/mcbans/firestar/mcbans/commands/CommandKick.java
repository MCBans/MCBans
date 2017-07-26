package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.Kick;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandKick extends BaseCommand{
    public CommandKick(){
        bePlayer = false;
        name = "kick";
        argLength = 1;
        usage = "kicks player from server";
        banning = true;
    }

    @Override
    public void execute() {
        args.remove(0); // remove target
        
        // build reason
        String reason = config.getDefaultKick();
        if (args.size() > 0){
            reason = Util.join(args, " ");
        }

        // Start
        Kick kickPlayer = new Kick(plugin, target, targetUUID, senderName, senderUUID, reason, false);
        Thread triggerThread = new Thread(kickPlayer);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.KICK.has(sender);
    }
}
