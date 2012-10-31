package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.pluginInterface.Kick;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandKick extends BaseCommand{
    public CommandKick(){
        bePlayer = false;
        name = "kick";
        argLength = 1;
        usage = "kick player";
        banning = false;
    }

    @Override
    public void execute() {
        String target = args.remove(0).trim();

        // build reason
        String reason = config.getString("defaultKick");
        if (args.size() > 0){
            reason = Util.join(args, " ");
        }

        // Start
        Kick kickPlayer = new Kick(config, plugin, target, senderName, reason);
        Thread triggerThread = new Thread(kickPlayer);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.KICK.has(sender);
    }
}
