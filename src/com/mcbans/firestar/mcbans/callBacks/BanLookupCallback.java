package com.mcbans.firestar.mcbans.callBacks;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.BanLookupData;
import com.mcbans.firestar.mcbans.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class BanLookupCallback extends BaseCallback{
    public BanLookupCallback(final MCBans plugin, final CommandSender sender) {
        super(plugin, sender);
    }

	// This Constructor is not used.
	public BanLookupCallback(){
		super(MCBans.getInstance(), null);
	}

    public void success(final BanLookupData data){
        String lostRep = (data.getLostRep() > 0) ? "&c" + data.getLostRep() : "&f0";
        String type = "&8(" + ((data.getType().contains("global")) ? "&4" : "&6") + data.getType().toUpperCase(Locale.ENGLISH) + "&8)";
        Util.message(sender, Util.color("&8------------------------------"));
        Util.message(sender, Util.color("&7Ban ID: &e#" + data.getBanID() + "&8 // &7Player: &c" + data.getPlayerName() + " " + type));
        Util.message(sender, Util.color("&7Server: &a" + data.getServer() + "&8 // &7Issued By: &a" + data.getAdminName()));
        Util.message(sender, Util.color("&7Rep Lost: " + lostRep + "&8 // &7Issued Date: &a" + data.getDate()));
        Util.message(sender, Util.color("&7Reason: &f" + data.getReason()));
        Util.message(sender, Util.color("&8------------------------------"));
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
