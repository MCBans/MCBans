package com.mcbans.firestar.mcbans.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.request.Ban;
import com.mcbans.firestar.mcbans.util.Util;

public class CommandTempban extends BaseCommand{
    public CommandTempban(){
        bePlayer = false;
        name = "tempban";
        argLength = 2;
        usage = "temporary ban player";
        banning = true;
    }

    @Override
    public void execute() {
        args.remove(0); // remove target

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
        
        String reason = config.getDefaultTemp();
        if (args.size() > 0){
            reason = Util.join(args, " ");
        }

        // Start
        Ban banControl = new Ban(plugin, BanType.TEMP.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, duration, measure, null, false);
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    @Override
    public boolean permission(CommandSender sender) {
        return BanType.TEMP.getPermission().has(sender);
    }
}
