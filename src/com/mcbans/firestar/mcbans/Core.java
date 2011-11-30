package com.mcbans.firestar.mcbans;

import com.mcbans.firestar.mcbans.log.LogLevels;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class Core {
	
	public String apikey;
	public String lang;
	
	public Core (BukkitInterface MCBans) {
		InputStream in;
		try {
			in = Core.class.getClassLoader().getResourceAsStream("core.yml");
		} catch (NullPointerException ex) {
            MCBans.log(LogLevels.FATAL, "MCBans: Unable to load core.yml!");
			return;
		}
		Yaml yaml = new Yaml();
		Map map = (Map)yaml.load(in);
		this.apikey = (String) map.get("apikey");
		this.lang = (String) map.get("lang");
		MCBans.log(LogLevels.INFO, "MCBans: Starting..");
	}
}