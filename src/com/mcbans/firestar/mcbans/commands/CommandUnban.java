package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.request.Ban;

public class CommandUnban extends BaseCommand{
    public CommandUnban(){
        bePlayer = false;
        name = "unban";
        argLength = 1;
        usage = "Unban player";
        banning = true;
    }

    @Override
    public void execute() {
        // Start
        Ban banControl = new Ban(plugin, BanType.UNBAN.getActionName(), target, "", senderName, "", "", "");
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.UNBAN.getPermission().has(sender);
    }
}
