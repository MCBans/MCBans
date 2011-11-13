package com.mcbans.firestar.mcbans;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.mcbans.firestar.mcbans.backup.backup;
import com.mcbans.firestar.mcbans.backup.backupCheck;
import com.mcbans.firestar.mcbans.bukkitListeners.playerListener;
import com.mcbans.firestar.mcbans.callBacks.mainCallBack;
import com.mcbans.firestar.mcbans.commands.commandHandler;
import com.mcbans.firestar.mcbans.log.ActionLog;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class bukkitInterface extends JavaPlugin {
	
	private commandHandler commandHandle; 
	private BukkitScheduler BScheduler;
	private final playerListener bukkitPlayer = new playerListener(this);
	public int taskID = 0;
	public HashMap<String, Integer> connectionData = new HashMap<String, Integer>();
	public HashMap<String, Long> resetTime = new HashMap<String, Long>();
	public Core Core = new Core();
	public Settings Settings;
	public Language Language = null;
	private mainCallBack callbackThread = null;
	private backupCheck backupThread = null;
	public ActionLog log = null;
	public backup Backup = null;
	public Consumer lbconsumer = null;
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
        	System.out.print("MCBans: Your server is not in online mode!");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }
        
        // API KEY verification!
        if (Core.apikey != null) {
        	this.apiKey = this.Core.apikey;
        	System.out.print("MCBans: Registered API Key (" + this.getApiKey() + ")");
        } else {
        	System.out.print("MCBans: Invalid MCBans.jar! Please re-download at http://myserver.mcbans.com.");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }
        
        Settings = new Settings(this);
        
        if (Settings.doTerminate) {
			System.out.print("MCBans: Please download the latest settings.yml from MCBans.com!");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
		}
        
		pm.registerEvent( Event.Type.PLAYER_JOIN, bukkitPlayer, Priority.Normal, this );
        pm.registerEvent( Event.Type.PLAYER_PRELOGIN, bukkitPlayer, Priority.Normal, this );
        pm.registerEvent( Event.Type.PLAYER_QUIT, bukkitPlayer, Priority.Normal, this );
        
        String language;
        
        if (Core.lang != null) {
        	language = Core.lang;
        } else {
        	System.out.print("MCBans: Invalid MCBans.jar! Please re-download at http://myserver.mcbans.com.");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }
        System.out.print("MCBans: Loading language file: "+language);
        
        File languageFile = new File("plugins/mcbans/language/"+language+".yml");
        if(!languageFile.exists()){
        	if (Core.lang != null) {
        		System.out.print("MCBans: Contacting Master server for language file " + Core.lang + ".yml");
        		Downloader getLanguage = new Downloader();
        		getLanguage.Download("http://myserver.mcbans.com/languages/" + Core.lang + ".yml", "plugins/mcbans/language/" + Core.lang + ".yml");
        		languageFile = new File("plugins/mcbans/language/" + Core.lang + ".yml");
        		if (!languageFile.exists()) {
        			System.out.print("MCBans: " + Core.lang + " does not exist on Master server.");
        			pm.disablePlugin(pluginInterface("mcbans"));
        		}
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
        
        
        if (Settings.getBoolean("throttleUsers")) {
        BScheduler = server.getScheduler();
        taskID = BScheduler.scheduleAsyncRepeatingTask(this, new ThrottleReset(this), 0L, 40L);
        
        	if (taskID == -1) {
        		log.write("Unable to schedule throttle reset task");
        		log.write("Throttling has been disabled.");
        	} else {
        		log.write("Connection throttling operating normally!");
        		log.write("Task ID: " + taskID);
        		log.write("Throttle Connect Limit: " + Settings.getInteger("userConnectionLimit"));
        	}
        }
        
        Plugin logBlock = pm.getPlugin("LogBlock");
        if (logBlock != null) {
        	lbconsumer = ((LogBlock)logBlock).getConsumer();
        	log.write("Enabling LogBlock intergration");
        }
        
        log.write("Started and operating normally!");
        
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
	
	public long getResetTime (String user) {
		if (!resetTime.containsKey(user)) {
			return 0;
		} else {
			return resetTime.get(user);
		}
	}
	
	public void setResetTime (String user, Long time) {
		resetTime.put(user, time);
	}
	
	public void clearThrottle (String user) {
		resetTime.remove(user);
		connectionData.remove(user);
	}
	
	public int getConnectionData (String user) {
		if (!connectionData.containsKey(user)) {
			return 0;
		} else {
			return connectionData.get(user);
		}
	}
	
	public void setConnectionData (String user, Integer count) {
		connectionData.put(user, count);
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