package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.firestar.mcbans.callBacks.PreviousCallback;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.PreviousNames;

public class CommandPrevious extends BaseCommand {
	public CommandPrevious(){
		bePlayer = false;
        name = "pvs";
        argLength = 1;
        usage = "pvs player";
        banning = true;
	}
	@Override
	public void execute() throws CommandException {
		args.remove(0);
		if (!this.permission(sender)){
            throw new CommandException(ChatColor.RED + _("permissionDenied"));
        }
		PreviousNames pN = new PreviousNames(plugin, new PreviousCallback(plugin, sender), target, targetUUID, senderName);
		Thread triggerThread = new Thread(pN);
        triggerThread.start();
	}

	@Override
	public boolean permission(CommandSender sender) {
		return Perms.VIEW_PREVIOUS.has(sender);
	}

}
