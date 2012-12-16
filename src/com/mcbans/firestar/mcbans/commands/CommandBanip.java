package com.mcbans.firestar.mcbans.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.callBacks.MessageCallback;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.BanIpRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandBanip extends BaseCommand{
    public CommandBanip(){
        bePlayer = false;
        name = "banip";
        argLength = 1;
        usage = "bans an IP address";
        banning = false;
    }

    @Override
    public void execute() {
        final String issuedBy = (sender instanceof Player) ? player.getName() : "Console";
        final String target = args.remove(0).trim();
        String reason = config.getDefaultLocal();
        if (args.size() > 0){
            reason = Util.join(args, " ");
        }
        
        // TODO check isValid IP address
        /*
        if (!Util.isValidName(target)){
            Util.message(sender, ChatColor.RED + _("invalidName"));
            return;
        }
        */

        // Start
        BanIpRequest request = new BanIpRequest(plugin, new MessageCallback(plugin, sender), target, reason, issuedBy);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.BAN_IP.has(sender);
    }
}
