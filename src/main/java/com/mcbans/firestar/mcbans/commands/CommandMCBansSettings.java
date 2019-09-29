package com.mcbans.firestar.mcbans.commands;


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.firestar.mcbans.callBacks.MCBansSettingsCallback;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.MCBansSettings;

import static com.mcbans.firestar.mcbans.I18n.localize;

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
			(new Thread(new MCBansSettings(plugin, new MCBansSettingsCallback(plugin, sender), sender.getName(), this.args.toString()))).start();
		}else{
			throw new CommandException(ChatColor.RED + localize("formatError"));
		}
	}

	@Override
	public boolean permission(CommandSender sender) {
		return Perms.ADMIN.has(sender);
	}

}
