package com.mcbans.plugin.commands;

import com.mcbans.plugin.request.PreviousNames;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.callBacks.PreviousCallback;
import com.mcbans.plugin.exception.CommandException;
import com.mcbans.plugin.permission.Perms;

import static com.mcbans.plugin.I18n.localize;

public class CommandPrevious extends BaseCommand {
	public CommandPrevious(){
		bePlayer = false;
        name = "namelookup";
        argLength = 1;
        usage = "nlup player";
        banning = true;
	}
	@Override
	public void execute() throws CommandException {
		args.remove(0);
		if (!this.permission(sender)){
            throw new CommandException(ChatColor.RED + localize("permissionDenied"));
        }
		(new Thread(new PreviousNames(plugin, new PreviousCallback(plugin, sender), target, targetUUID, senderName))).start();
	}

	@Override
	public boolean permission(CommandSender sender) {
		return Perms.VIEW_PREVIOUS.has(sender);
	}

}
