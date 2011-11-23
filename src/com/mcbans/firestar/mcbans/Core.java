package com.mcbans.firestar.mcbans;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class Core {
	
	public String apikey;
	public String lang;
	
	public Core () {
		InputStream in;
		try {
			in = Core.class.getClassLoader().getResourceAsStream("core.yml");
		} catch (NullPointerException ex) {
            System.out.print("MCBans: Unable to load core.yml!");
			return;
		}
		Yaml yaml = new Yaml();
		Map map = (Map)yaml.load(in);
		this.apikey = (String) map.get("apikey");
		this.lang = (String) map.get("lang");
		System.out.print("MCBans: Starting..");
	}
}