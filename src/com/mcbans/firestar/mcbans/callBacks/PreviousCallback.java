package com.mcbans.firestar.mcbans.callBacks;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class PreviousCallback extends BaseCallback{

	public PreviousCallback(MCBans plugin, CommandSender sender) {
		super(plugin, sender);
		
	}
	
	@Override
	public void success(String identifier, String playerlist ) {
		if(!playerlist.equals("")){
			Util.message(sender,  ChatColor.RED +_("previousNamesHas", I18n.PLAYER, identifier, I18n.PLAYERS, playerlist));
		}else{
			Util.message(sender,  ChatColor.AQUA +_("previousNamesNone", I18n.PLAYER, identifier));
		}
	}
	
	@Override
	public void success() {
		throw new IllegalArgumentException("Wrong Usage!");
	}

	@Override
	public void error(String error) {
		if (error != null && sender != null){
            Util.message(sender, ChatColor.RED + error);
        }
	}

}
