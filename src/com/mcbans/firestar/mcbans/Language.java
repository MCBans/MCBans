package com.mcbans.firestar.mcbans;

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
	public String getFormatMessageView( String Message, String Sender, String Date, String message ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%SENDER%", Sender).replaceAll("%DATE%", Date).replaceAll("%MESSAGE%", message);
	}
	public String getFormat( String Message, String PlayerName ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName);
	}
	public String getFormatCount( String Message, String Count ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%COUNT%", Count);
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
	public String getFormat( String Message, String PlayerName, String PlayerAdmin, String Reason, String PlayerIP, String Word ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName).replaceAll("%PLAYERIP%", PlayerIP).replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason).replaceAll("%BADWORD%", Word);
	}
	public String getFormatAlts( String Message, String PlayerName, String AltList ){
		return config.getString( Message, "No language file loaded!" ).replaceAll("%PLAYER%", PlayerName).replaceAll("%ALTS%", AltList);
	}
}