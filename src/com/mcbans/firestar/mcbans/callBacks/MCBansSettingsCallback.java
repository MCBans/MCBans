package com.mcbans.firestar.mcbans.callBacks;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class MCBansSettingsCallback extends BaseCallback {
	public MCBansSettingsCallback(MCBans plugin, CommandSender sender) {
		super(plugin, sender);
		
	}
	@Override
	public void success(String returnData, String reason) {
		if(returnData.equalsIgnoreCase("y")){
			Util.message(sender, ChatColor.GREEN +_("successSetting", I18n.REASON, reason));
		}else{
			Util.message(sender, ChatColor.RED +_("failSetting", I18n.REASON, reason));
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
