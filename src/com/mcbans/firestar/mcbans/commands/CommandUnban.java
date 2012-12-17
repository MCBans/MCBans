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
        usage = "Unban player or ip";
        banning = false;
    }

    @Override
    public void execute() throws CommandException {
        String target = args.get(0).trim();
        
        if (!Util.isValidName(target) && !Util.isValidIP(target)){
            throw new CommandException(ChatColor.RED + _("invalidNameOrIP"));
        }
        
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
