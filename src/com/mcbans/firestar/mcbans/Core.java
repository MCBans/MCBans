package com.mcbans.firestar.mcbans;

import java.io.*;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class Core {
	
	public String apikey;
	public String lang;
	
	public Core () {
		InputStream in;
		try {
			in = Core.class.getClassLoader().getResourceAsStream("core.yml");
		} catch (NullPointerException ex) {
			return;
		}
		Yaml yaml = new Yaml();
		Map map = (Map)yaml.load(in);
		this.apikey = (String) map.get("apikey");
		this.lang = (String) map.get("lang");
		System.out.print("MCBans: Set Core API Key and Language");
	}
}
