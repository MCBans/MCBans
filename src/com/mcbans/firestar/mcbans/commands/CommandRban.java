package com.mcbans.firestar.mcbans.commands;

import static com.mcbans.firestar.mcbans.I18n._;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.exception.CommandException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.Ban;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandRban extends BaseCommand{
    public CommandRban(){
        bePlayer = false;
        name = "rban";
        argLength = 1;
        usage = "ban player and rollback";
        banning = true;
    }

    @Override
    public void execute() throws CommandException {
        args.remove(0); // remove target

        // check BanType
        BanType type = BanType.LOCAL;
        if (args.size() > 0) {
            if (args.get(0).equalsIgnoreCase("g")){
                type = BanType.GLOBAL;
            }else if (args.get(0).equalsIgnoreCase("t")){
                type = BanType.TEMP;
            }
        }
        if (type != BanType.LOCAL){
            args.remove(0);
        }

        // check permission
        if (!type.getPermission().has(sender)){
            throw new CommandException(ChatColor.RED + _("permissionDenied"));
        }

        // check hasRollbackMethod
        if (!plugin.getRbHandler().hasRollbackMethod()){
            throw new CommandException(ChatColor.RED + _("rbMethodNotFound"));
        }

        String reason = null;
        Ban banControl = null;
		switch (type){
            case LOCAL:
                reason = config.getDefaultLocal();
                if (args.size() > 0){
                    reason = Util.join(args, " ");
                }
                banControl = new Ban(plugin, type.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, "", "", (new JSONObject()), true);
                break;

            case GLOBAL:
                if (args.size() == 0){
                    Util.message(sender, ChatColor.RED + _("formatError"));
                    return;
                }
                reason = Util.join(args, " ");
                banControl = new Ban(plugin, type.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, "", "", (new JSONObject()), true);
                break;

            case TEMP:
                if (args.size() <= 2){
                    Util.message(sender, ChatColor.RED + _("formatError"));
                    return;
                }
                String measure = "";
                String duration = args.remove(0);
                if(!duration.matches("(?sim)([0-9]+)(minute(s|)|m|hour(s|)|h|day(s|)|d|week(s|)|w)")){
                	measure = args.remove(0);
                }else{
                	try {
                		Pattern regex = Pattern.compile("([0-9]+)(minute(s|)|m|hour(s|)|h|day(s|)|d|week(s|)|w)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
                		Matcher regexMatcher = regex.matcher(duration);
                		if (regexMatcher.find()) {
                			duration = regexMatcher.group(1);
                			measure = regexMatcher.group(2);
                		} 
                	} catch (PatternSyntaxException ex) {}
                }
                reason = config.getDefaultTemp();
                if (args.size() > 0){
                    reason = Util.join(args, " ");
                }
                banControl = new Ban(plugin, type.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, duration, measure, (new JSONObject()), true);
                break;
        }

        // Start
        if (banControl == null){
            Util.message(sender, ChatColor.RED + "Internal error! Please report console logs!");
            throw new RuntimeException("Undefined BanType: " + type.name());
        }
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return Perms.BAN_ROLLBACK.has(sender);
    }
}
