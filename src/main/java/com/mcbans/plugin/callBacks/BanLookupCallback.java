package com.mcbans.plugin.callBacks;

import java.util.Locale;

import com.mcbans.domain.models.client.Ban;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.util.Util;

public class BanLookupCallback extends BaseCallback{
    public BanLookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }
    public BanLookupCallback(){
        super(MCBans.getInstance(), null);
    }

    public void success(final Ban ban){
        String lostRep = (ban.getType().equals("global") && ban.getServer().getReputation() > 0) ? "&c" + ban.getServer().getReputation() : "&f0";
        String type = "&8(" + ((ban.getType().contains("global")) ? "&4" : "&6") + ban.getType().toUpperCase(Locale.ENGLISH) + "&8)";
        Util.messages(sender, new String[]{
          Util.color("&8------------------------------"),
          Util.color("&7Ban ID: &e#" + ban.getId() + "&8 // &7Player: &c" + ban.getPlayer().getName() + " " + type),
          Util.color("&7Server: &a" + ban.getServer().getAddress() + "&8 // &7Issued By: &a" + ((ban.getAdmin()!=null)?ban.getAdmin().getName():"console")),
          Util.color("&7Rep Lost: " + lostRep + "&8 // &7Issued Date: &a" + ban.getDate()),
          Util.color("&7Reason: &f" + StringEscapeUtils.unescapeHtml(ban.getReason())),
          Util.color("&8------------------------------")
        });
    }

    @Override
    public void success(){
        throw new IllegalArgumentException("Wrong Usage!");
    }

    @Override
    public void error(final String error){
        if (error != null && sender != null){
            Util.message(sender, ChatColor.RED + error);
        }
    }
}
