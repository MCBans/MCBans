package com.mcbans.firestar.mcbans.commands;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.request.Ban;
import org.bukkit.command.CommandSender;

public class CommandUnban extends BaseCommand{
    public CommandUnban(){
        bePlayer = false;
        name = "unban";
        argLength = 1;
        usage = "unban a player/uuid or IP";
        banning = true;
    }

    @Override
    public void execute() throws CommandException {
        //TODO CommandException is never thrown in the method.

    	args.remove(0); // remove target
        //String target = args.get(0).trim(); already fetched in BaseCommand
        
        // Start
        Ban banControl = new Ban(plugin, BanType.UNBAN.getActionName(), target, targetUUID, "", senderName, senderUUID, "", "", "", null, false);
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.UNBAN.getPermission().has(sender);
    }
}
