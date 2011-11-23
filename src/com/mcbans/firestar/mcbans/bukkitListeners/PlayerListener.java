package com.mcbans.firestar.mcbans.bukkitListeners;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.Connect;
import com.mcbans.firestar.mcbans.pluginInterface.Disconnect;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
	private BukkitInterface MCBans;
	public PlayerListener(BukkitInterface plugin) {
        MCBans = plugin;
    }
	@Override
	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
		if (event.getResult() != Result.ALLOWED) {
			return;
		}
		String playerIP = event.getAddress().getHostAddress();
        String playerName = event.getName();
		Connect playerConnect = new Connect( MCBans );
		String result = playerConnect.exec( playerName, playerIP );
		if( result != null ){
			event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, result);
		}
	}
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerName = event.getPlayer().getName();
		if(MCBans.Settings.getBoolean("onJoinMCBansMessage")){
			MCBans.broadcastPlayer( event.getPlayer(), ChatColor.DARK_GREEN + "Server secured by MCBans!" );
		}
		if(MCBans.joinMessages.containsKey(playerName)){
			for(String message : MCBans.joinMessages.get(playerName)){
				MCBans.broadcastPlayer( event.getPlayer(),  message );
			}
			MCBans.joinMessages.remove(playerName);
        }
	}
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        Disconnect disconnectHandler = new Disconnect( MCBans, playerName );
        disconnectHandler.start();
    }
}