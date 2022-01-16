package com.mcbans.plugin.callBacks;

import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.api.data.BanLookupData;
import com.mcbans.plugin.util.Util;

public class BanLookupCallback extends BaseCallback{
    public BanLookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }
    public BanLookupCallback(){
        super(MCBans.getInstance(), null);
    }

    public void success(final BanLookupData data){
        String lostRep = (data.getLostRep() > 0) ? "&c" + data.getLostRep() : "&f0";
        String type = "&8(" + ((data.getType().contains("global")) ? "&4" : "&6") + data.getType().toUpperCase(Locale.ENGLISH) + "&8)";
        Util.messages(sender, new String[]{
          Util.color("&8------------------------------"),
          Util.color("&7Ban ID: &e#" + data.getBanID() + "&8 // &7Player: &c" + data.getPlayerName() + " " + type),
          Util.color("&7Server: &a" + data.getServer() + "&8 // &7Issued By: &a" + data.getAdminName()),
          Util.color("&7Rep Lost: " + lostRep + "&8 // &7Issued Date: &a" + data.getDate()),
          Util.color("&7Reason: &f" + data.getReason()),
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
