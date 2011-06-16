package com.firestar.mcbans;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.firestar.mcbans.bukkitListeners.playerListener;
import com.firestar.mcbans.commands.commandHandler;
import com.firestar.mcbans.log.ActionLog;

public class bukkitInterface extends JavaPlugin {
	private commandHandler commandHandle; 
	private final playerListener bukkitPlayer = new playerListener(this);
	public Settings Settings = new Settings("settings.yml");
	public Language Language = null;
	public ActionLog log = null;
	public bukkitPermissions Permissions = null;
	public void onDisable() {
		System.out.print("MCBans: Disabled");
	}
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, bukkitPlayer, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_PRELOGIN, bukkitPlayer, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, bukkitPlayer, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, bukkitPlayer, Priority.Normal, this);
        Permissions = new bukkitPermissions( Settings, this );
        commandHandle = new commandHandler( Settings, this );
        Permissions.setupPermissions();
        System.out.print("MCBans: Loading language file: "+Settings.getString("language"));
        if(Settings.getBoolean("logEnable")){
        	System.out.print("MCBans: Starting to save to log file!");
        	log = new ActionLog( this, Settings.getString("logFile") );
        	log.write( "MCBans Log File Started" );
        }else{
        	log = new ActionLog( this, "" );
        	System.out.print("MCBans: Log file disabled!");
        }
        Language = new Language(Settings.getString("language"));
        System.out.print("MCBans: Now Active!");
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return commandHandle.execCommand( command.getName(), args, sender );
	}
	public void broadcastBanView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			if( Permissions.isAllow( player.getWorld().getName(), player.getName(), "ban.view" ) ){
				player.sendMessage( Settings.getString("prefix")+" "+msg );
			}
		}
	}
	public void broadcastAll(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			player.sendMessage( Settings.getString("prefix")+" "+msg );
		}
	}
	public void broadcastPlayer( String Player, String msg ){
		Player target = this.getServer().getPlayer(Player);
		if(target!=null){
			target.sendMessage( Settings.getString("prefix") + " " + msg );
		}else{
			System.out.print( Settings.getString("prefix") + " " + msg );
		}
	}
	public void broadcastPlayer( Player target, String msg ){
		target.sendMessage( Settings.getString("prefix") + " " + msg );
	}
	public Plugin pluginInterface( String pluginName ){
		return this.getServer().getPluginManager().getPlugin(pluginName);
	}
}