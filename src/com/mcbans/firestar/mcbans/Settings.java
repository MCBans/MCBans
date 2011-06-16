package com.mcbans.firestar.mcbans;

import java.io.File;

import org.bukkit.util.config.Configuration;

public class Settings{
	private Configuration config;
	public Settings( String filename ){
		File plugin_settings = new File("plugins/mcbans/"+filename);
		config = new Configuration(plugin_settings);
		config.load();
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