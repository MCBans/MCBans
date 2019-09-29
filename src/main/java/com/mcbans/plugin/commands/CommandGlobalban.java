package com.mcbans.plugin.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.plugin.BanType;
import com.mcbans.plugin.request.Ban;
import com.mcbans.plugin.util.Util;


public class CommandGlobalban extends BaseCommand{
    public CommandGlobalban(){
        bePlayer = false;
        name = "globalban";
        argLength = 2;
        usage = "global ban a player";
        banning = true;
    }

    @Override
    public void execute() {
        args.remove(0); // remove target

        // build reason
        String reason = Util.join(args, " ");
        
        // Start
        Ban banControl = new Ban(plugin, BanType.GLOBAL.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, "", "", null, false);
        banControl.run();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.GLOBAL.getPermission().has(sender);
    }
}
