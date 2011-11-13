package com.mcbans.firestar.mcbans;

import java.io.File;

import com.mcbans.firestar.mcbans.bukkitInterface;
import org.bukkit.configuration.file.*;

public class Settings{
	private bukkitInterface MCBans;
	private YamlConfiguration config;
	private YamlConfiguration backupConfig;
	public boolean doTerminate = false;
	
	public Settings( bukkitInterface p ){
		MCBans = p;
		File plugin_settings = new File("plugins/mcbans/settings.yml");
		if (!plugin_settings.exists()) {
			System.out.print("MCBans: settings.yml not found, downloading default..");
			Downloader download = new Downloader();
			download.Download("http://myserver.mcbans.com/getSettings/" + MCBans.getApiKey(), "plugins/mcbans/settings.yml");
			plugin_settings = new File("plugins/mcbans/settings.yml");
			if (!plugin_settings.exists()) {
				System.out.print("MCBans: Unable to download settings.yml!");
				this.doTerminate = true;
			} else {
				config = YamlConfiguration.loadConfiguration(plugin_settings);
			}
		} else {
			config = YamlConfiguration.loadConfiguration(plugin_settings);
		}		
		if (!verifyIntegrity()) {
			System.out.print("MCBans: settings.yml is corrupted! One or more variables are missing/invalid!");
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
			if (verifyIntegrity()) {
				return 1;
			} else {
				config = backupConfig;
				return -1;
			}
		}
	}
	@SuppressWarnings("unused")
	private boolean verifyIntegrity () {
		if (getString("prefix") == null) {
			System.out.print("MCBans: Invalid prefix!");
			return false;
		} else if (getString("defaultLocal") == null) {
			System.out.print("MCBans: Invalid defaultLocal!");
			return false;
		} else if (getString("defaultTemp") == null) {
			System.out.print("MCBans: Invalid defaultTemp!");
			return false;
		} else if (getString("defaultKick") == null) {
			System.out.print("MCBans: Invalid defaultKick!");
			return false;
		} else if (getString("offlineReason") == null) {
			System.out.print("MCBans: Invalid offlineReason!");
			return false;
		} else if (getString("userLockoutMsg") == null) {
			System.out.print("MCBans: Invalid userLockoutMsg!");
			return false;
		} else if (getString("allLockoutMsg") == null) {
			System.out.print("MCBans: Invalid allLockoutMsg!");
			return false;
		} else if (getString("logFile") == null) {
			System.out.print("MCBans: Invalid logFile!");
			return false;
		} else if (getString("onJoinMCBansMessage") == null) {
			System.out.print("MCBans: Invalid onJoinMCBansMessage!");
			return false;
		} else if (getString("enableMaxAlts") == null) {
			System.out.print("MCBans: Invalid enableMaxAlts!");
			return false;
		} else if (getString("throttleUsers") == null) {
			System.out.print("MCBans: Invalid throttleUsers!");
			return false;
		} else if (getString("throttleAll") == null) { 
			System.out.print("MCBans: Invalid throttleAll!");
			return false;
		} else if (getString("isDebug") == null) { 
			System.out.print("MCBans: Invalid isDebug!");
			return false;
		} else if (getString("logEnable") == null) {
			System.out.print("MCBans: Invalid logEnable!");
			return false;
		} else {
			try {
				int minRep = Integer.parseInt(getString("minRep"));
				int callBack = Integer.parseInt(getString("callBackInterval"));
				int maxAlts = Integer.parseInt(getString("maxAlts"));
				int userCTime = Integer.parseInt(getString("userConnectionTime"));
				int userCLimit = Integer.parseInt(getString("userConnectionLimit"));
				int userLockout = Integer.parseInt(getString("userLockoutTime"));
				int allCTime = Integer.parseInt(getString("allConnectionTime"));
				int allCLimit = Integer.parseInt(getString("allConnectionLimit"));
				int allLockout = Integer.parseInt(getString("allLockoutTime"));
			} catch (NumberFormatException nFE) {
				return false;
			}
		}
		return true;
	}
	public String getString( String variable ){
		return config.getString( variable, "" );
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