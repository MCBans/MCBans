package com.mcbans.firestar.mcbans;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Settings {
    private YamlConfiguration config;
    public boolean exists = false;

    public Settings() {
        File plugin_settings = new File("plugins/mcbans/settings.yml");
        YamlConfiguration configTest = null;
        if (!plugin_settings.exists()) {
            System.out.print("MCBans: settings.yml not found, generating default..");
            this.generate();
            plugin_settings = new File("plugins/mcbans/settings.yml");
            configTest = YamlConfiguration.loadConfiguration(plugin_settings);
        } else {
            configTest = YamlConfiguration.loadConfiguration(plugin_settings);
        }
        String verify = verifyIntegrity(configTest);
        if (verify != "OK") {
            System.out.print("[FATAL][MCBans] settings.yml is corrupted! One or more variables are missing/invalid! (" + verify + ")");
            this.exists = true;
        } else {
            config = configTest;
        }
    }

    public void generate() {
        InputStream in = null;
        try {
            in = Settings.class.getClassLoader().getResourceAsStream("defaults/settings.yml");
            File file = new File("plugins/mcbans/");
            if (!file.exists()) {
                file.mkdir();
            }
            file = new File("plugins/mcbans/settings.yml");
            OutputStream out = new FileOutputStream(file);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println("Error writing settings file.");
        }
    }

    public Integer reload() {
        File plugin_settings = new File("plugins/mcbans/settings.yml");
        if (!plugin_settings.exists()) {
            return -2;
        } else {
            YamlConfiguration configTest = YamlConfiguration.loadConfiguration(plugin_settings);
            String verify = verifyIntegrity(configTest);
            if (verify == "OK") {
                config = configTest;
                return 1;
            } else {
                return -1;
            }
        }
    }

    private String verifyIntegrity(YamlConfiguration test) {
        if (test.getString("prefix", "").equals("")) {
            return "prefix";
        } else if (!test.isString("defaultLocal")) {
            return "defaultLocal";
        } else if (!test.isString("defaultTemp")) {
            return "defaultTemp";
        } else if (!test.isString("defaultKick")) {
            return "defaultKick";
        } else if (!test.isString("logFile")) {
            return "logFile";
        } else if (!test.isString("apiKey")) {
            return "apiKey";
        } else if (!test.isString("language")) {
            return "language";
        } else if (!test.isBoolean("onJoinMCBansMessage")) {
            return "onJoinMCBansMessage";
        } else if (!test.isBoolean("enableMaxAlts")) {
            return "enableMaxAlts";
        } else if (!test.isBoolean("isDebug")) {
            return "isDebug";
        } else if (!test.isBoolean("logEnable")) {
            return "logEnable";
        }
        return "OK";
    }

    public String getString(String variable) {
        return config.getString(variable, "");
    }

    public String getPrefix() {
        return config.get("prefix", "").toString();
    }

    public Integer getInteger(String variable) {
        return config.getInt(variable, 0);
    }

    public boolean getBoolean(String variable) {
        return config.getBoolean(variable, true);
    }

    public double getDouble(String variable) {
        return config.getDouble(variable, 1.00);
    }

    public float getFloat(String variable) {
        return Float.valueOf(config.getString(variable, "0.00"));
    }
}