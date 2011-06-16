package com.firestar.mcbans;

import java.io.File;

import org.bukkit.util.config.Configuration;

public class Language{
	private Configuration config;
	public Language( String filename ){
		File plugin_settings = new File("plugins/mcbans/language/"+filename+".yml");
		config = new Configuration(plugin_settings);
		config.load();
	}
	public String getFormat( String Message ){
		return config.getString( Message, "No language file loaded!" );
	}
	public String getFormat( String Message, String PlayerName){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin, String Reason ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason);
	}
	public String getFormat( String Message, String PlayerName, String PlayerAdmin, String Reason, String PlayerIP ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName).replaceAll("%PLAYERIP%", PlayerIP).replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason);
	}
}