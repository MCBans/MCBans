package com.mcbans.plugin.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.mcbans.plugin.MCBans;
import org.bukkit.command.CommandSender;

import com.mcbans.plugin.BanType;
import com.mcbans.plugin.request.Ban;
import com.mcbans.plugin.util.Util;

public class CommandTempban extends BaseCommand {
  public CommandTempban() {
    bePlayer = false;
    name = "tempban";
    argLength = 2;
    usage = "temporary ban a player";
    banning = true;
  }

  @Override
  protected List<String> tabComplete(MCBans plugin, CommandSender sender, String cmd, String[] preArgs) {
    switch (preArgs.length) {
      case 1:
        return plugin.getServer().getOnlinePlayers().stream().map(p -> p.getName()).filter(p -> p.startsWith(preArgs[0])).collect(Collectors.toList());
      case 2:
        if (preArgs[1].matches("([0-9]+)")) {
          return new ArrayList() {{
            add(preArgs[1] + "seconds");
            add(preArgs[1] + "minutes");
            add(preArgs[1] + "hours");
            add(preArgs[1] + "days");
            add(preArgs[1] + "weeks");
          }};
        } else if (preArgs[1].equals("")) {
          return new ArrayList() {{
            add("30minutes");
            add("5hours");
            add("1day");
            add("1week");
          }};
        }
    }
    return new ArrayList<>();
  }

  @Override
  public void execute() {
    args.remove(0); // remove target
    if (args.size() < 1) {
      Util.message(sender, "Command incomplete.");
      return;
    }
    String measure = "";
    String duration = args.remove(0);
    if (duration.matches("(?sim)([0-9]+)(minute(s|)|m|second(s|)|s|hour(s|)|h|day(s|)|d|week(s|)|w)")) {
      try {
        Pattern regex = Pattern.compile("([0-9]+)(minute(s|)|m|second(s|)|s|hour(s|)|h|day(s|)|d|week(s|)|w)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher regexMatcher = regex.matcher(duration);
        if (regexMatcher.find()) {
          duration = regexMatcher.group(1);
          measure = regexMatcher.group(2);
        }
      } catch (PatternSyntaxException ex) {
      }
    }

    String reason = config.getDefaultTemp();
    if (args.size() > 0) {
      reason = Util.join(args, " ");
    }

    // Start
    Ban banControl = new Ban(plugin, BanType.TEMP.getActionName(), target, targetUUID, targetIP, senderName, senderUUID, reason, duration, measure, null, false);
    banControl.run();
  }

  @Override
  public boolean permission(CommandSender sender) {
    return BanType.TEMP.getPermission().has(sender);
  }
}
