package com.mcbans.plugin.commands;


import com.mcbans.plugin.request.MCBansSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.callBacks.MCBansSettingsCallback;
import com.mcbans.plugin.exception.CommandException;
import com.mcbans.plugin.permission.Perms;

import static com.mcbans.plugin.I18n.localize;

public class CommandMCBansSettings extends BaseCommand {
	public CommandMCBansSettings(){
        bePlayer = false;
        name = "mcbs";
        argLength = 0;
        usage = "mcbs <setting> <value>";
        banning = true;
    }
	@Override
	public void execute() throws CommandException {
		if (!this.permission(sender)){
            throw new CommandException(ChatColor.RED + localize("permissionDenied"));
        }
		if(this.args.size()>=2){
			new Thread(new MCBansSettings(plugin, new MCBansSettingsCallback(plugin, sender), sender.getName(), this.args.toString())).start();
		}else{
			throw new CommandException(ChatColor.RED + localize("formatError"));
		}
	}

	@Override
	public boolean permission(CommandSender sender) {
		return Perms.ADMIN.has(sender);
	}

}
