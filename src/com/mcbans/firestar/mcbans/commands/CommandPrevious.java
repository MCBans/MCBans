package com.mcbans.firestar.mcbans.commands;

import com.mcbans.firestar.mcbans.callBacks.PreviousCallback;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.PreviousNames;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import static com.mcbans.firestar.mcbans.I18n.localize;

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
