package com.mcbans.mcbans.pluginInterface;

import com.mcbans.mcbans.BukkitInterface;
import com.mcbans.mcbans.Settings;
import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public class Kick extends Thread {
	private Settings Config;
	private BukkitInterface MCBans;
	private String PlayerName = null;
	private String PlayerAdmin = null;
	private String Reason = null;
	public Kick(Settings cf, BukkitInterface p, String playerName, String playerAdmin, String reason){
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