package com.mcbans.firestar.mcbans;

import com.mcbans.firestar.mcbans.log.LogLevels;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Settings{
	private BukkitInterface MCBans;
	private YamlConfiguration config;
	private YamlConfiguration backupConfig;
	public boolean doTerminate = false;
	private String NFe = null;
	
	public Settings( BukkitInterface p ){
		MCBans = p;
		File plugin_settings = new File("plugins/mcbans/settings.yml");
		if (!plugin_settings.exists()) {
            MCBans.useColor = false;
            MCBans.log(LogLevels.INFO, "settings.yml not found, downloading default..");
			Downloader download = new Downloader();
			download.Download("http://myserver.mcbans.com/getSettings/" + MCBans.getApiKey(), "plugins/mcbans/settings.yml");
			plugin_settings = new File("plugins/mcbans/settings.yml");
			if (!plugin_settings.exists()) {
				MCBans.log(LogLevels.FATAL, "Unable to download settings.yml!");
                return;
			} else {
				config = YamlConfiguration.loadConfiguration(plugin_settings);
			}
		} else {
			config = YamlConfiguration.loadConfiguration(plugin_settings);
		}		
		String verify = verifyIntegrity();
		if (verify != "OK") {
			System.out.print("MCBans: settings.yml is corrupted! One or more variables are missing/invalid! (" + verify + ")");
			if (NFe != null) {
				System.out.print("MCBans: " + NFe);
			}
			this.doTerminate = true;
		}
	}
	public Integer reload() {
		File plugin_settings = new File("plugins/mcbans/settings.yml");
		if (!plugin_settings.exists()) {
			return -2;
		} else {
			backupConfig = config;
			config = YamlConfiguration.loadConfiguration(plugin_settings);
			String verify = verifyIntegrity();
			if (verify == "OK") {
				return 1;
			} else {
				config = backupConfig;
				return -1;
			}
		}
	}
	private String verifyIntegrity () {
		if (getPrefix() == "") {
			return "prefix";
		} else if (!config.isString("defaultLocal")) {
			return "defaultLocal";
		} else if (!config.isString("defaultTemp")) {
			return "defaultTemp";
		} else if (!config.isString("defaultKick")) {
			return "defaultKick";
		} else if (!config.isString("offlineReason")) {
			return "offlineReason";
		} else if (!config.isString("userLockoutMsg")) {
			return "userLockoutMsg";
		} else if (!config.isString("allLockoutMsg")) {
			return "allLockoutMsg";
		} else if (!config.isString("logFile")) {
			return "logFile";
		} else if (!config.isBoolean("onJoinMCBansMessage")) {
			return "onJoinMCBansMessage";
		} else if (!config.isBoolean("enableMaxAlts")) {
			return "enableMaxAlts";
		} else if (!config.isBoolean("throttleUsers")) {
			return "throttleUsers";
		} else if (!config.isBoolean("throttleAll")) { 
			return "throttleAll";
		} else if (!config.isBoolean("isDebug")) { 
			return "isDebug";
		} else if (!config.isBoolean("logEnable")) {
			return "logEnable";
        } else if (!config.isBoolean("enableColor")) {
            config.addDefault("enableColor", true);
            config.set("enableColor", true);
		} else {
			try {
				Integer.parseInt(getInteger("minRep").toString());
				Integer.parseInt(getInteger("callBackInterval").toString());
				Integer.parseInt(getInteger("maxAlts").toString());
				Integer.parseInt(getInteger("userConnectionTime").toString());
				Integer.parseInt(getInteger("userConnectionLimit").toString());
				Integer.parseInt(getInteger("userLockoutTime").toString());
				Integer.parseInt(getInteger("allConnectionTime").toString());
				Integer.parseInt(getInteger("allConnectionLimit").toString());
				Integer.parseInt(getInteger("allLockoutTime").toString());
			} catch (NumberFormatException nFE) {
				NFe = nFE.toString();
				return "numberError";
			}
		}
		return "OK";
	}
	public String getString( String variable ){
		return config.getString( variable, "" );
	}
	public String getPrefix () {
		return config.get("prefix", "").toString();
	}
	public Integer getInteger( String variable ){
		return config.getInt( variable, 0 );
	}
	public boolean getBoolean( String variable ){
		return config.getBoolean( variable, true );
	}
	public double getDouble( String variable ){
		return config.getDouble( variable, 1.00 );
	}
	public float getFloat( String variable ){
		return Float.valueOf(config.getString(variable, "0.00" ));
	}
}