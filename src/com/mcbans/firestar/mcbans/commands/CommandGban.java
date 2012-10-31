package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;
import com.mcbans.firestar.mcbans.util.Util;


public class CommandGban extends BaseCommand{
    public CommandGban(){
        bePlayer = false;
        name = "gban";
        argLength = 2;
        usage = "global ban player";
        banning = true;
    }

    @Override
    public void execute() {
        args.remove(0); // remove target

        // build reason
        String reason = Util.join(args, " ");

        // Start
        Ban banControl = new Ban(plugin, BanType.GLOBAL.getActionName(), target, targetIP, senderName, reason, "", "");
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.GLOBAL.getPermission().has(sender);
    }
}
