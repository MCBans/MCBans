package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.callBacks.MessageCallback;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.BanIpRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandBanip extends BaseCommand{
    public CommandBanip(){
        bePlayer = false;
        name = "banip";
        argLength = 1;
        usage = "bans an IP address";
        banning = true;
    }

    @Override
    public void execute() throws CommandException {
    	args.remove(0); // remove target
    	
        String reason = config.getDefaultLocal();
        if (args.size() > 0){
            reason = Util.join(args, " ");
        }
        
        // check isValid IP address
        if (!Util.isValidIP(target)){
            throw new CommandException(ChatColor.RED + _("invalidIP"));
        }

        // Start
        BanIpRequest request = new BanIpRequest(plugin, new MessageCallback(plugin, sender), target, reason, senderName, senderUUID);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.BAN_IP.has(sender);
    }
}
