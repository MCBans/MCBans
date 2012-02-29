package com.mcbans.firestar.mcbans;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.Map;

public class Core {
	
	public String apikey;
	public String lang;
	
	@SuppressWarnings("rawtypes")
	public Core () {
		InputStream in;
		try {
			in = Core.class.getClassLoader().getResourceAsStream("core.yml");
		} catch (NullPointerException ex) {
            // System.out.print("MCBans: Unable to load core.yml!");
			return;
		}

        try {
            Yaml yaml = new Yaml();
            Map map = (Map)yaml.load(in);
            this.apikey = (String) map.get("apikey");
            this.lang = (String) map.get("lang");
        } catch (YAMLException e) {
            // Means it's not there, but we're ok with that
        }

		System.out.print("MCBans: Starting..");
	}
}