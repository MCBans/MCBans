package com.mcbans.firestar.mcbans;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public class BukkitPermissions {
	private BukkitInterface MCBans;
	
	private Settings Config;
	public BukkitPermissions(Settings cf, BukkitInterface p){
		MCBans = p;
		Config=cf;
	}
	public boolean isAllow( String WorldName, String PlayerName, String PermissionNode ){
		Player target = MCBans.getServer().getPlayer(PlayerName);
		return target != null && isAllow( target, PermissionNode );
	}
	public boolean isAllow( Player Player, String PermissionNode ){
		if( Player.hasPermission( "mcbans."+PermissionNode ) ){
			return true;
		}
		return false;
	}
}