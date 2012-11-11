package com.mcbans.firestar.mcbans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import uk.co.oliwali.HawkEye.util.Util;

import com.google.common.base.Joiner;
import com.mcbans.firestar.mcbans.util.FileStructure;

public class I18n {
    private static final String languageDirName = "languages";

    private static Configuration fallbackMessages;
    private static Configuration messages;

    /**
     * Init I18n
     * @param locale set current locale
     */
    public static void init(final String locale){
        // extract languages
        extractLanguageFiles(false);

        // load default (fallback) language: English
        try{
            fallbackMessages = loadLanguageFile("en-us");
        }catch (Exception ex){
            System.out.print("Could not load default(en-us.yml) messages file!");
        }

        // load custom language
        try{
            setCurrentLanguage(locale);
        }catch (Exception ex){
            System.out.print("Could not load messages for " + locale + ": using en-us.yml");
            messages = fallbackMessages;
        }
    }

    // Extract methods
    private static void extractLanguageFiles(final boolean force){
        final File langDir = getLanguagesDir();
        FileStructure.createDir(langDir);

        // extract resources
        List<String> locales = new ArrayList<String>();

        locales.add("en-us");
        locales.add("dutch");
        locales.add("french");
        locales.add("german");
        locales.add("italian");
        locales.add("ja-jp");
        locales.add("norwegian");
        locales.add("polish");
        locales.add("portuguese");
        locales.add("spanish");
        locales.add("sv-se");

        for (String locale : locales){
            FileStructure.extractResource("/languages/" + locale + ".yml", langDir, force, true);
        }
    }

    // Load methods
    public static void setCurrentLanguage(final String locale) throws Exception{
        messages = loadLanguageFile(locale);
    }
    private static Configuration loadLanguageFile(final String locale) throws Exception{
        final File langDir = getLanguagesDir();
        File file = new File(langDir, locale + ".yml");

        // check file available
        if (file == null || !file.isFile() || !file.canRead()){
            System.out.print("Unknown language file: " + locale);
            return null;
        }

        YamlConfiguration conf =  YamlConfiguration.loadConfiguration(file);

        // check all messages available
        if (fallbackMessages != null && conf.getKeys(true).size() != fallbackMessages.getKeys(true).size()){
            // collect missing message keys
            for (String key : fallbackMessages.getKeys(true)){
                if (!conf.contains(key) && !fallbackMessages.isConfigurationSection(key)){
                    conf.set(key, fallbackMessages.get(key));
                    System.out.print("[MCBans] Missing message key on " + locale + ".yml: " + key);
                }
            }
        }
        return conf;
    }

    /* ***** Begin Replace methods ***** */
    public static final String PLAYER = "%PLAYER%";
    public static final String SENDER = "%ADMIN%";
    public static final String REASON = "%REASON%";
    public static final String BADWORD = "%BADWORD%";
    public static final String ALTS = "%ALTS%";
    public static final String PLAYERIP = "%PLAYERIP%";
    /* ***** End Replace methods ******* */

    public static String _(final String key){
        // message file not proper loaded
        if (messages == null){
            System.out.print("[MCBans] Localized messages file is NOT loaded..");
            return "!" + key + "!";
        }

        String msg = getString(messages, key);

        if (msg == null || msg.length() == 0){
            if (msg == null) System.out.print("[MCBans] Missing message key '" + key + "'");
            msg = getString(fallbackMessages, key);
            if (msg == null || msg.length() == 0) msg = "!" + key + "!";
        }

        return msg;
    }

    private static String getString(final Configuration conf, final String key){
        String s = null;
        Object o = conf.get(key);

        if (o instanceof String){
            s = o.toString();
        }
        else if (o instanceof List<?>){
            @SuppressWarnings("unchecked")
            List<String> l = (List<String>) o;
            s = Util.join(l, "\n");
        }

        return s;
    }

    /**
     * Get languages directory
     * @return File
     */
    private static File getLanguagesDir(){
        return new File(FileStructure.getPluginDir(), languageDirName);
    }
}
