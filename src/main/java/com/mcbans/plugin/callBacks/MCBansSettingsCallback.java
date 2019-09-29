package com.mcbans.plugin.callBacks;

import static com.mcbans.plugin.I18n.localize;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import com.mcbans.plugin.I18n;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.util.Util;

public class MCBansSettingsCallback extends BaseCallback {
	public MCBansSettingsCallback(MCBans plugin, CommandSender sender) {
		super(plugin, sender);
		
	}
	@Override
	public void success(String returnData, String reason) {
		if(returnData.equalsIgnoreCase("y")){
			Util.message(sender, ChatColor.GREEN +localize("successSetting", I18n.REASON, reason));
		}else{
			Util.message(sender, ChatColor.RED +localize("failSetting", I18n.REASON, reason));
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
