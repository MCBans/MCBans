package com.mcbans.firestar.mcbans;

import com.mcbans.firestar.mcbans.backup.Backup;
import com.mcbans.firestar.mcbans.backup.BackupCheck;
import com.mcbans.firestar.mcbans.bukkitListeners.PlayerListener;
import com.mcbans.firestar.mcbans.callBacks.MainCallBack;
import com.mcbans.firestar.mcbans.commands.CommandHandler;
import com.mcbans.firestar.mcbans.log.ActionLog;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.log.Logger;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import org.bukkit.ChatColor;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BukkitInterface extends JavaPlugin {
	
	private CommandHandler commandHandle;
	private final PlayerListener bukkitPlayer = new PlayerListener(this);
	public int taskID = 0;
	public HashMap<String, Integer> connectionData = new HashMap<String, Integer>();
	public HashMap<String, Long> resetTime = new HashMap<String, Long>();
    public Logger logger = new Logger(this);
	public Core Core = null;
	public Settings Settings;
	public Language Language = null;
	public MainCallBack callbackThread = null;
	private BackupCheck backupThread = null;
	public ActionLog actionLog = null;
	public Backup Backup = null;
	public Consumer lbconsumer = null;
	private String apiKey = "";
	private boolean mode = false;
    public boolean useColor = true;
	public BukkitPermissions Permissions = null;
    private String gitRevision = "@@GITREVISION@@";
    private String buildVersion = "@@BUILDVERSION@@";
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

        useColor = Settings.getBoolean("enableColor");

        if (!buildVersion.contains("BUILDVERSION") && !gitRevision.contains("GITREVISION")) {
            log(LogLevels.INFO, "Running MCBans v" + getDescription().getVersion() + " git-" + gitRevision + " b" + buildVersion + "bamboo");
        }
        if (useColor) {
            log (LogLevels.INFO, ChatColor.GREEN + "This is version of MCBans is sporting a colorful interface!");
            log (LogLevels.INFO, "To disable it, set enableColor to false in the settings.yml");
        }

		PluginManager pm = getServer().getPluginManager();
		
		//Rigby's Help :D
		CraftServer server = (CraftServer) getServer();
		
        boolean isFirestarFail = server.getServer().onlineMode;
        if( !isFirestarFail ){
        	logger.log(LogLevels.FATAL, "MCBans: Your server is not in online mode!");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }

        Core = new Core(this);
        
        // API KEY verification!
        if (Core.apikey != null) {
        	this.apiKey = this.Core.apikey;
        	log("Core loaded successfully!");
        } else {
        	log(LogLevels.FATAL, "Invalid MCBans.jar! Please re-download at http://myserver.mcbans.com.");
        	return;
        }
        
        Settings = new Settings(this);
        
        if (Settings.doTerminate) {
			log(LogLevels.FATAL, "Please download the latest settings.yml from MCBans.com!");
        	return;
		}
        
		pm.registerEvent( Event.Type.PLAYER_JOIN, bukkitPlayer, Priority.Normal, this );
        pm.registerEvent( Event.Type.PLAYER_PRELOGIN, bukkitPlayer, Priority.Normal, this );
        pm.registerEvent( Event.Type.PLAYER_QUIT, bukkitPlayer, Priority.Normal, this );
        
        String language;
        
        if (Core.lang != null) {
        	language = Core.lang;
        } else {
        	log(LogLevels.FATAL, "Invalid MCBans.jar! Please re-download at http://myserver.mcbans.com.");
        	return;
        }
        log(LogLevels.INFO, "Loading language file: "+language);
        
        File languageFile = new File("plugins/mcbans/language/"+language+".yml");
        if(!languageFile.exists()){
        	if (Core.lang != null) {
        		log(LogLevels.INFO, "Contacting Master server for language file " + Core.lang + ".yml");
        		Downloader getLanguage = new Downloader();
        		getLanguage.Download("http://myserver.mcbans.com/languages/" + Core.lang + ".yml", "plugins/mcbans/language/" + Core.lang + ".yml");
        		languageFile = new File("plugins/mcbans/language/" + Core.lang + ".yml");
        		if (!languageFile.exists()) {
        			log(LogLevels.FATAL, Core.lang + " does not exist on Master server.");
                    return;
        		}
        	} else {
        		log(LogLevels.FATAL, "No language file found!");
        		return;
        	}
        }
        
        if(Settings.getBoolean("logEnable")){
        	log(LogLevels.INFO, "Starting to save to log file!");
        	actionLog = new ActionLog( this, Settings.getString("logFile") );
        	actionLog.write("MCBans Log File Started");
        }else{
        	log(LogLevels.INFO, "Log file disabled!");
        }
        
        Permissions = new BukkitPermissions( Settings, this );
        commandHandle = new CommandHandler( Settings, this );
        Permissions.setupPermissions();
        
        log("Fetching backup.");
        Backup = new Backup( Settings.getBoolean("isDebug"), this.getApiKey() );
        Backup.fetch();
        
        log("Starting MCBans online check.");
        backupThread = new BackupCheck( this );
        backupThread.start();
        
        callbackThread = new MainCallBack( this );
        callbackThread.start();
        if (Core.lang != null) {
        	Language = new Language(Core.lang);
        } else {
        	Language = new Language(Settings.getString("language"));
        }
        
        
        if (Settings.getBoolean("throttleUsers")) {
        BukkitScheduler BScheduler = server.getScheduler();
        taskID = BScheduler.scheduleAsyncRepeatingTask(this, new ThrottleReset(this), 0L, 40L);
        
        	if (taskID == -1) {
        		log(LogLevels.SEVERE, "Unable to schedule throttle reset task");
        		log(LogLevels.SEVERE, "Throttling has been disabled.");
        	} else {
        		log(LogLevels.INFO, "Connection throttling operating normally!");
        		log(LogLevels.INFO, "Task ID: " + taskID);
        		log(LogLevels.INFO, "Throttle Connect Limit: " + Settings.getInteger("userConnectionLimit"));
        	}
        }
        
        Plugin logBlock = pm.getPlugin("LogBlock");
        if (logBlock != null) {
        	lbconsumer = ((LogBlock)logBlock).getConsumer();
        	log(LogLevels.INFO, "Enabling LogBlock integration");
        }

        log(LogLevels.INFO, "Started and operating normally!");
        
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return commandHandle.execCommand( command.getName(), args, sender );
	}

    public void log(String message) {
        log(LogLevels.NONE, message);
    }

    public void log(LogLevels type, String message) {
        if (actionLog != null) {
            actionLog.write(message);
        }
        logger.log(type, message);
    }
	
	public void broadcastBanView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			if( Permissions.isAllow( player.getWorld().getName(), player.getName(), "ban.view" ) ){
				player.sendMessage( Settings.getPrefix() + " " + msg );
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
			player.sendMessage( Settings.getPrefix() + " " + msg );
		}
	}
	
	public void broadcastPlayer( String Player, String msg ){
		Player target = this.getServer().getPlayer(Player);
		if(target!=null){
			target.sendMessage( Settings.getPrefix() + " " + msg );
		}else{
			this.getServer().getConsoleSender().sendMessage( ChatColor.AQUA + Settings.getPrefix() +  " " + ChatColor.WHITE + msg );
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
		target.sendMessage( Settings.getPrefix() + " " + msg );
	}
	
	public boolean hasErrored (HashMap<String, String> response) {
		if (response.containsKey("error")) {
			String error = response.get("error");
			if (error.contains("Server Disabled")) {
				if (getMode()) {
					return true;
				}
				broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
				log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
				setMode(true);
			} else if (error.contains("api key not found.")) {
				broadcastBanView( ChatColor.RED + "Invalid MCBans.jar!");
				broadcastBanView("The API key inside the current MCBans.jar is invalid. Please re-download the plugin from myserver.mcbans.com");
				log(LogLevels.FATAL, "Invalid MCBans.jar - Please re-download from myserver.mcbans.com!");
			} else {
				broadcastBanView( ChatColor.RED + "Unexpected reply from MCBans API!");
				log(LogLevels.SEVERE, "API returned an invalid error:");
				log(LogLevels.SEVERE, "MCBans said: " + error);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasErrored (String response) {
		if (response.contains("error")) {
			if (response.contains("Server Disabled")) {
				if (getMode()) {
					return true;
				}
				broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
				log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
				setMode(true);
			} else if (response.contains("api key not found.")) {
				broadcastBanView( ChatColor.RED + "Invalid MCBans.jar!");
				broadcastBanView( "The API key inside the current MCBans.jar is invalid. Please re-download the plugin from myserver.mcbans.com");
				log(LogLevels.FATAL, "Invalid MCBans.jar - Please re-download from myserver.mcbans.com!");
				getServer().getPluginManager().disablePlugin(pluginInterface("mcbans"));
			} else {
				broadcastBanView( ChatColor.RED + "Unexpected reply from MCBans API!");
				log(LogLevels.SEVERE, "API returned an invalid error:");
				log(LogLevels.SEVERE, "MCBans said: " + response);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public Plugin pluginInterface( String pluginName ){
		return this.getServer().getPluginManager().getPlugin(pluginName);
	}
	
}