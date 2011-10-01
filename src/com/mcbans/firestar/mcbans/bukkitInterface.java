package com.mcbans.firestar.mcbans;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcbans.firestar.mcbans.backup.backup;
import com.mcbans.firestar.mcbans.backup.backupCheck;
import com.mcbans.firestar.mcbans.bukkitListeners.playerListener;
import com.mcbans.firestar.mcbans.callBacks.mainCallBack;
import com.mcbans.firestar.mcbans.commands.commandHandler;
import com.mcbans.firestar.mcbans.log.ActionLog;

public class bukkitInterface extends JavaPlugin {
	
	private commandHandler commandHandle; 
	private final playerListener bukkitPlayer = new playerListener(this);
	public Settings Settings = new Settings("settings.yml");
	public Core Core = new Core();
	public Language Language = null;
	private mainCallBack callbackThread = null;
	private backupCheck backupThread = null;
	public ActionLog log = null;
	public backup Backup = null;
	private String apiKey = "";
	private boolean mode = false;
	public bukkitPermissions Permissions = null;
	public HashMap<String, ArrayList<String>> joinMessages = new HashMap<String, ArrayList<String>>();
	
	public void onDisable() {
		System.out.print("MCBans: Disabled");
		if(callbackThread!=null){
			if(callbackThread.isAlive()){
				callbackThread.interrupt();
			}
		}
		if(backupThread!=null){
			if(backupThread.isAlive()){
				backupThread.interrupt();
			}
		}
	}
	
	public void onEnable() {
		
		PluginManager pm = getServer().getPluginManager();
		
		//Rigby's Help :D
		CraftServer server = (CraftServer) getServer();
        boolean isFirestarFail = server.getServer().onlineMode;
        if( !isFirestarFail ){
        	System.out.print("MCBans: Can only run in online mode!");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }
        
        // API KEY verification!
        if (Core.apikey != null) {
        	apiKey = this.Core.apikey;
        } else {
        	apiKey = this.Settings.getString("apiKey");
        }
        if(apiKey.equalsIgnoreCase("<changeme>")){
        	System.out.print("MCBans: You need to enter your api key! You can find it at http://myserver.mcbans.com.");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }
        try {
        	if (apiKey.matches("(?i)<(.*?)>")) {
        		System.out.print("MCBans: Remove the < and > from the api key, it is not needed!");
            	pm.disablePlugin(pluginInterface("mcbans"));
            	return;
        	} 
        } catch (PatternSyntaxException ex) {
        }

        
		pm.registerEvent( Event.Type.PLAYER_JOIN, bukkitPlayer, Priority.Normal, this );
        pm.registerEvent( Event.Type.PLAYER_PRELOGIN, bukkitPlayer, Priority.Normal, this );
        pm.registerEvent( Event.Type.PLAYER_CHAT, bukkitPlayer, Priority.Highest, this );
        pm.registerEvent( Event.Type.PLAYER_QUIT, bukkitPlayer, Priority.Normal, this );
        
        System.out.print("MCBans: Loading language file: "+Settings.getString("language"));
        
        File languageFile = new File("plugins/mcbans/language/"+Settings.getString("language")+".yml");
        if(!languageFile.exists()){
        	if (Core.lang != null) {
        		System.out.print("MCBans: Contacting Master server for language file " + Core.lang + ".yml");
        		Core.download("http://myserver.mcbans.com/languages/en-us.yml", "plugins/mcbans/language/en-us.yml");
        		languageFile = new File("plugins/mcbans/language/en-us.yml");
        	} else {
        		System.out.print("MCBans: No language file found!");
        		pm.disablePlugin(pluginInterface("mcbans"));
        		return;
        	}
        }
        
        if(Settings.getBoolean("logEnable")){
        	System.out.print("MCBans: Starting to save to log file!");
        	log = new ActionLog( this, Settings.getString("logFile") );
        	log.write( "MCBans Log File Started" );
        }else{
        	log = new ActionLog( this, "" );
        	System.out.print("MCBans: Log file disabled!");
        }
        
        Permissions = new bukkitPermissions( Settings, this );
        commandHandle = new commandHandler( Settings, this );
        Permissions.setupPermissions();
        
        log.write("Fetching backup.");
        Backup = new backup( this.Settings.getBoolean("isDebug"), this.getApiKey() );
        Backup.fetch();
        
        log.write("Starting MCBans online check.");
        backupThread = new backupCheck( this );
        backupThread.start();
        
        callbackThread = new mainCallBack( this );
        callbackThread.start();
        if (Core.lang != null) {
        	Language = new Language(Core.lang);
        } else {
        	Language = new Language(Settings.getString("language"));
        }
        log.write("Started normally.");
        
	}
	
	@Override
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
	public boolean getMode(){
		return mode;
	}
	public void setMode( boolean newMode ){
		mode = newMode;
	}
	public String getApiKey(){
		return this.apiKey;
	}
	public void broadcastPlayer( Player target, String msg ){
		target.sendMessage( Settings.getString("prefix") + " " + msg );
	}
	
	public Plugin pluginInterface( String pluginName ){
		return this.getServer().getPluginManager().getPlugin(pluginName);
	}
	
}