package com.mcbans.firestar.mcbans.callBacks;

import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.BanLookupData;
import com.mcbans.firestar.mcbans.util.Util;

public class BanLookupCallback extends BaseCallback{
    public BanLookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }
    public BanLookupCallback(){
        super(MCBans.getInstance(), null);
    }

    public void success(final BanLookupData data){
        String lostRep = (data.getLostRep() > 0) ? "&4" + data.getLostRep() : "&70";
        String type = "&f[" + ((data.getType().contains("global")) ? "&c" : "&6") + data.getType().toUpperCase(Locale.ENGLISH) + "&f]";

        Util.message(sender, Util.color("Ban ID: #&a" + data.getBanID() + "&f Player: &3" + data.getPlayerName() + " " + type));
        Util.message(sender, Util.color("Server: &3" + data.getServer() + "&f Issued By: &3" + data.getAdminName()));
        Util.message(sender, Util.color("Rep Lost: " + lostRep + "&f Issued Date: &3" + data.getDate()));
        Util.message(sender, Util.color("Reason: &3" + data.getReason()));
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
