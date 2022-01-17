package com.mcbans.plugin.commands;

import com.mcbans.plugin.MCBans;
import org.bukkit.command.CommandSender;

import com.mcbans.plugin.BanType;
import com.mcbans.plugin.request.Ban;
import com.mcbans.plugin.util.Util;

import java.util.List;
import java.util.stream.Collectors;


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
    protected List<String> tabComplete(MCBans plugin, CommandSender sender, String cmd, String[] preArgs) {
        if(preArgs.length==1){
            return plugin.getServer().getOnlinePlayers().stream().filter(player->player.getName().startsWith(preArgs[0])).map(player->player.getName()).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.GLOBAL.getPermission().has(sender);
    }
}
