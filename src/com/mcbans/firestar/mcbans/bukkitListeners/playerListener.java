package com.mcbans.firestar.mcbans.bukkitListeners;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcbans.firestar.mcbans.bukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.connect;
import com.mcbans.firestar.mcbans.pluginInterface.disconnect;

public class playerListener extends PlayerListener {
	private bukkitInterface MCBans;
	public playerListener(bukkitInterface plugin) {
        MCBans = plugin;
    }
	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
		String playerIP = event.getAddress().getHostAddress();
        String playerName = event.getName();
		connect playerConnect = new connect( MCBans );
		String result = playerConnect.exec( playerName, playerIP );
		if( result != null ){
			event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, result);
		}
	}
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(MCBans.Settings.getBoolean("onJoinMCBansMessage")){
			MCBans.broadcastPlayer( event.getPlayer(), ChatColor.DARK_GREEN + "Server secured by MCBans!" );
		}
	}
	public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        disconnect disconnectHandler = new disconnect( MCBans, playerName );
        disconnectHandler.run();
    }
}