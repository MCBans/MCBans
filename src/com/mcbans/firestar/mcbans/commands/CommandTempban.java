package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.request.Ban;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandTempban extends BaseCommand{
    public CommandTempban(){
        bePlayer = false;
        name = "tempban";
        argLength = 3;
        usage = "temporary ban player";
        banning = true;
    }

    @Override
    public void execute() {
        args.remove(0); // remove target

        final String duration = args.remove(0);
        final String measure = args.remove(0);
        String reason = config.getDefaultTemp();
        if (args.size() > 0){
            reason = Util.join(args, " ");
        }

        // Start
        Ban banControl = new Ban(plugin, BanType.TEMP.getActionName(), target, targetIP, senderName, reason, duration, measure);
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.TEMP.getPermission().has(sender);
    }
}
