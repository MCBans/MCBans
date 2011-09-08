package com.mcbans.firestar.mcbans.pluginInterface;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.bukkitInterface;

@SuppressWarnings("unused")
public class kick extends Thread {
	private Settings Config;
	private bukkitInterface MCBans;
	private String PlayerName = null;
	private String PlayerAdmin = null;
	private String Reason = null;
	public kick( Settings cf, bukkitInterface p, String playerName, String playerAdmin, String reason ){
		Config=cf;
		MCBans = p;
		PlayerName = playerName;
		PlayerAdmin = playerAdmin;
		Reason = reason;
	}
	@Override
	public void run( ){
		
		if (MCBans.getServer().getPlayer(PlayerName) != null) {
			MCBans.log.write( PlayerAdmin + " has kicked " + PlayerName + "[" + Reason + "]" );
			MCBans.getServer().getPlayer(PlayerName).kickPlayer(MCBans.Language.getFormat( "kickMessagePlayer", PlayerName, PlayerAdmin, Reason ));
			MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "kickMessageSuccess", PlayerName, PlayerAdmin, Reason ));
        }else{
        	MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "kickMessageNoPlayer", PlayerName, PlayerAdmin, Reason ));
        }
		
	}
}