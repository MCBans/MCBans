package com.mcbans.firestar.mcbans.pluginInterface;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.bukkitInterface;
import org.bukkit.entity.Player;

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
		Player player = MCBans.getServer().getPlayer(PlayerName);
		if (player != null) {
			MCBans.log.write( PlayerAdmin + " has kicked " + player.getName() + "[" + Reason + "]" );
			player.kickPlayer(MCBans.Language.getFormat( "kickMessagePlayer", player.getName(), PlayerAdmin, Reason ));
			MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "kickMessageSuccess", player.getName(), PlayerAdmin, Reason ));
		}else{
        		MCBans.broadcastAll( ChatColor.DARK_RED + MCBans.Language.getFormat( "kickMessageNoPlayer", PlayerName, PlayerAdmin, Reason ));
        	}
		
	}
}