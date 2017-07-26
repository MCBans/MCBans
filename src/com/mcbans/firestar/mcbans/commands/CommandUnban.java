package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.request.Ban;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandUnban extends BaseCommand{
    public CommandUnban(){
        bePlayer = false;
        name = "unban";
        argLength = 1;
        usage = "Unbans the player, uuid, or ip";
        banning = true;
    }

    @Override
    public void execute() throws CommandException {
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
