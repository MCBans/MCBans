package com.mcbans.firestar.mcbans;

import java.io.File;

import com.mcbans.firestar.mcbans.bukkitInterface;
import org.bukkit.configuration.file.*;

public class Settings{
	private bukkitInterface MCBans;
	private YamlConfiguration config;
	public boolean doTerminate = false;
	
	public Settings( String filename, bukkitInterface p ){
		MCBans = p;
		File plugin_settings = new File("plugins/mcbans/"+filename);
		if (!plugin_settings.exists()) {
			System.out.print("MCBans: " + filename + " not found, downloading default..");
			Downloader download = new Downloader();
			download.Download("http://myserver.mcbans.com/getSettings/" + MCBans.getApiKey(), "plugins/mcbans/"+filename);
			plugin_settings = new File("plugins/mcbans/"+filename);
			if (!plugin_settings.exists()) {
				System.out.print("MCBans: Unable to download " + filename + "!");
				this.doTerminate = true;
			} else {
				config = YamlConfiguration.loadConfiguration(plugin_settings);
			}
		} else {
			config = YamlConfiguration.loadConfiguration(plugin_settings);
		}		
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