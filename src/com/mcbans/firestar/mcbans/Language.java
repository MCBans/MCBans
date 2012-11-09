package com.mcbans.firestar.mcbans;

import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.YamlConfiguration;

public class Language {
    private MCBans plugin;
    private YamlConfiguration config;

    public Language(MCBans mcbans) {
        plugin = mcbans;
        InputStream in = null;
        try {
            in = Language.class.getClassLoader().getResourceAsStream("languages/" + plugin.getConfigs().getLanguage() + ".yml");
        } catch (NullPointerException ex) {
        }
        config = YamlConfiguration.loadConfiguration(in);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String errorMessage(String Message) {
        if (Message == null) {
            return "Missing language file!";
        } else {
            return "Missing language variable: " + Message;
        }
    }

    public boolean reload() {
        InputStream in;
        try {
            in = Language.class.getClassLoader().getResourceAsStream("languages/" + plugin.getConfigs().getLanguage() + ".yml");
        } catch (NullPointerException ex) {
            return false;
        }
        config = YamlConfiguration.loadConfiguration(in);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getFormat(String Message) {
        return config.getString(Message, this.errorMessage(Message));
    }

    public String getFormatMessageView(String Message, String Sender, String Date, String message) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%SENDER%", Sender).replaceAll("%DATE%", Date)
                .replaceAll("%MESSAGE%", message);
    }

    public String getFormat(String Message, String PlayerName) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%PLAYER%", PlayerName);
    }

    public String getFormatCount(String Message, String Count) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%COUNT%", Count);
    }

    public String getFormat(String Message, String PlayerName, String PlayerAdmin) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin);
    }

    public String getFormat(String Message, String PlayerName, String PlayerAdmin, String Reason) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin)
                .replaceAll("%REASON%", Reason);
    }

    public String getFormat(String Message, String PlayerName, String PlayerAdmin, String Reason, String defaultMessage, boolean meow) {
        return config.getString(Message, defaultMessage).replaceAll("%PLAYER%", PlayerName).replaceAll("%ADMIN%", PlayerAdmin)
                .replaceAll("%REASON%", Reason);
    }

    public String getFormat(String Message, String PlayerName, String PlayerAdmin, String Reason, String PlayerIP) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%PLAYER%", PlayerName).replaceAll("%PLAYERIP%", PlayerIP)
                .replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason);
    }

    public String getFormat(String Message, String PlayerName, String PlayerAdmin, String Reason, String PlayerIP, String Word) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%PLAYER%", PlayerName).replaceAll("%PLAYERIP%", PlayerIP)
                .replaceAll("%ADMIN%", PlayerAdmin).replaceAll("%REASON%", Reason).replaceAll("%BADWORD%", Word);
    }

    public String getFormatAlts(String Message, String PlayerName, String AltList) {
        return config.getString(Message, this.errorMessage(Message)).replaceAll("%PLAYER%", PlayerName).replaceAll("%ALTS%", AltList);
    }
}