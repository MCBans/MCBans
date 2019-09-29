package com.mcbans.plugin.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.BanType;
import com.mcbans.plugin.exception.CommandException;
import com.mcbans.plugin.request.Ban;
import com.mcbans.plugin.util.Util;

import static com.mcbans.plugin.I18n.localize;

public class CommandBan extends BaseCommand{
    public CommandBan(){
        bePlayer = false;
        name = "ban";
        argLength = 1;
        usage = "ban a player";
        banning = true;
    }

    @Override
    public void execute() throws CommandException {
        args.remove(0); //remove target

        // check BanType
        BanType type = BanType.LOCAL;
        if (args.size() > 0) {
            if (args.get(0).equalsIgnoreCase("-g")){
                type = BanType.GLOBAL;
            }else if (args.get(0).equalsIgnoreCase("-t")){
                type = BanType.TEMP;
            }
        }
        if (type != BanType.LOCAL){
            args.remove(0);
        }

        // check permission
        if (!type.getPermission().has(sender)){
            throw new CommandException(ChatColor.RED + localize("permissionDenied"));
        }

        String reason = null;
        Ban banControl = null;
        switch (type){
            case LOCAL:
                reason = config.getDefaultLocal();
                if (args.size() > 0){
                    reason = Util.join(args, " ");
                }
                banControl = new Ban(plugin, type.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, "", "", null, false);
                break;

            case GLOBAL:
                if (args.size() == 0){
                    Util.message(sender, ChatColor.RED + localize("formatError"));
                    return;
                }
                reason = Util.join(args, " ");
                banControl = new Ban(plugin, type.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, "", "", null, false);
                break;

            case TEMP:
                if (args.size() < 2){
                    Util.message(sender, ChatColor.RED + localize("formatError"));
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
                banControl = new Ban(plugin, type.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, duration, measure, null, false);
                break;
        }

        // Start
        if (banControl == null){
            Util.message(sender, ChatColor.RED + "Internal error. Please report console logs to an MCBans developer.");
            throw new RuntimeException("Undefined BanType: " + type.name());
        }
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return (BanType.GLOBAL.getPermission().has(sender) ||
                BanType.LOCAL.getPermission().has(sender)  ||
                BanType.TEMP.getPermission().has(sender));
    }
}
