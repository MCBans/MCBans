package com.mcbans.plugin.commands;

import com.mcbans.plugin.request.BanIpRequest;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.callBacks.MessageCallback;
import com.mcbans.plugin.exception.CommandException;
import com.mcbans.plugin.permission.Perms;
import com.mcbans.plugin.util.Util;

import static com.mcbans.plugin.I18n.localize;

public class CommandBanip extends BaseCommand{
    public CommandBanip(){
        bePlayer = false;
        name = "banip";
        argLength = 1;
        usage = "ban an IP address";
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
            throw new CommandException(ChatColor.RED + localize("invalidIP"));
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
