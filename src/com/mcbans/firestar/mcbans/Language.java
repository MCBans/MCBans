package com.mcbans.firestar.mcbans;

import java.io.File;

import org.bukkit.configuration.file.*;

public class Language{
	private bukkitInterface MCBans;
	private YamlConfiguration config;
	private YamlConfiguration backupConfig;
	public Language( String filename ){
		File plugin_settings = new File("plugins/mcbans/language/"+filename+".yml");
		config = YamlConfiguration.loadConfiguration(plugin_settings);
	}
	private String errorMessage ( String Message ) {
		if (Message == null) {
			return "Missing language file!";
		} else {
			return "Missing language variable: " + Message;
		}
	}
	public boolean reload () {
		File languageFile = new File("plugins/mcbans/language/" + MCBans.Core.lang + ".yml");
		if (!languageFile.exists()) {
			return false;
		} else {
			config = YamlConfiguration.loadConfiguration(languageFile);
			return true;
		}
	}
	public String getFormat( String Message ){
		return config.getString( Message, this.errorMessage(Message) );
	}
	public String getFormatMessageView( String Message, String Sender, String Date, String message ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%SENDER%", Sender).replaceAll("%DATE%", Date).replaceAll("%MESSAGE%", message);
	}
	public String getFormat( String Message, String PlayerName ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%PLAYER%", PlayerName);
	}
	public String getFormatCount( String Message, String Count ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%COUNT%", Count);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin, String Reason ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin, String Reason, String PlayerIP ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%PLAYER%", PlayerName).replaceAll("%PLAYERIP%", PlayerIP).replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin, String Reason, String PlayerIP, String Word ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%PLAYER%", PlayerName).replaceAll("%PLAYERIP%", PlayerIP).replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason).replaceAll("%BADWORD%", Word);
	}
	public String getFormatAlts( String Message, String PlayerName, String AltList ){
		return config.getString( Message, this.errorMessage(Message) ).replaceAll("%PLAYER%", PlayerName).replaceAll("%ALTS%", AltList);
	}
}