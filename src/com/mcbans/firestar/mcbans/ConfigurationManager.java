package com.mcbans.firestar.mcbans;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.mcbans.firestar.mcbans.util.FileStructure;
import com.mcbans.firestar.mcbans.util.Util;


public class ConfigurationManager {
    /* Current config.yml File Version! */
    private final int latestVersion = 2;

    private final MCBans plugin;
    private final ActionLog log;

    //private YamlConfiguration conf;
    private FileConfiguration conf;
    private File pluginDir;
    
    private boolean isValidKey = false;

    /**
     * Constructor
     */
    public ConfigurationManager(final MCBans plugin){
        this.plugin = plugin;
        this.log = plugin.getLog();

        this.pluginDir = this.plugin.getDataFolder();
    }

    /**
     * Load config.yml
     */
    public void loadConfig(final boolean initialLoad) throws Exception{
        // create directories
        FileStructure.createDir(pluginDir);

        // get config.yml path
        File file = new File(pluginDir, "config.yml");
        if (!file.exists()){
            FileStructure.extractResource("/config.yml", pluginDir, false, false);
            log.log(Level.INFO, "config.yml has not been found! We created a default config.yml for you!", false);
        }

        plugin.reloadConfig();
        conf = plugin.getConfig();

        checkver(conf.getInt("ConfigVersion", 1));

        // check API key
        if (conf.getString("apiKey", "").trim().length() != 40){
            isValidKey = false;
            if (initialLoad){
                Util.message((CommandSender)null, ChatColor.RED + "=== Missing OR Invalid API Key! ===");
                log.severe("MCBans detected a missing or invalid API Key!");
                log.severe("Please copy your API key to the configuration file.");
                log.severe("Don't have an API key? Go to: http://my.mcbans.com/servers/");
                //plugin.getPluginLoader().disablePlugin(plugin); // Don't disable plugin
                //return;
            }else{
                log.severe("MCBans detected a missing or invalid API Key! Please check config.yml!");
            }
        }else{
            isValidKey = true;
        }

        // check log enable
        if (isEnableLog()){
            if (!new File(getLogFile()).exists()){
                try{
                    new File(getLogFile()).createNewFile();
                } catch (IOException ex){
                    log.warning("Could not create log file! " + getLogFile());
                }
            }
        }

        // check isEnabledAutoSync
        if (!initialLoad && isEnableAutoSync()){
            plugin.bansync.goRequest(); // force run auto-sync
        }
    }

    /**
     * Check configuration file version
     */
    private void checkver(final int ver){
        // compare configuration file version
        if (ver < latestVersion){
            // first, rename old configuration
            final String destName = "oldconfig-v" + ver + ".yml";
            String srcPath = new File(pluginDir, "config.yml").getPath();
            String destPath = new File(pluginDir, destName).getPath();
            try{
                FileStructure.copyTransfer(srcPath, destPath);
                log.info("Outdated config file! Automatically copied old config.yml to " + destName + "!");
            }catch(Exception ex){
                log.warning("Failed to copy old config.yml!");
            }

            // force copy config.yml and languages
            FileStructure.extractResource("/config.yml", pluginDir, true, false);
            //Language.extractLanguageFile(true);

            plugin.reloadConfig();
            conf = plugin.getConfig();

            log.info("Deleted existing configuration file and generate a new one!");
        }
    }
    
    public boolean isValidApiKey(){
        return isValidKey;
    }

    /* ***** Begin Configuration Getters *********************** */
    public String getPrefix(){
        return Util.color(conf.getString("prefix", "[MCBans]"));
    }
    public String getApiKey(){
        if (!isValidKey){
            Util.message((CommandSender)null, ChatColor.RED + "Invalid API Key! Edit your config.yml and type /mcbans reload");
            return "";
        }
        return conf.getString("apiKey", "").trim();
    }
    public String getLanguage(){
        return conf.getString("language", "default");
    }
    public String getPermission(){
        return conf.getString("permission", "SuperPerms");
    }

    public String getDefaultLocal(){
        return conf.getString("defaultLocal", "You have been banned!");
    }
    public String getDefaultTemp(){
        return conf.getString("defaultTemp", "You have been temporarily banned!");
    }
    public String getDefaultKick(){
        return conf.getString("defaultKick", "You have been kicked!");
    }

    public boolean isDebug(){
        return conf.getBoolean("isDebug", false);
    }
    public boolean isEnableLog(){
        return conf.getBoolean("logEnable", false);
    }
    public String getLogFile(){
        return conf.getString("logFile", "plugins/MCBans/actions.log");
    }

    public boolean isEnableMaxAlts(){
        return conf.getBoolean("enableMaxAlts", false);
    }
    public int getMaxAlts(){
        return conf.getInt("maxAlts", 2);
    }

    public String getAffectedWorlds(){
        return conf.getString("affectedWorlds", "*");
    }
    public int getBackDaysAgo(){
        return conf.getInt("backDaysAgo", 20);
    }

    public boolean isEnableAutoSync(){
        return conf.getBoolean("enableAutoSync", true);
    }
    public int getSyncInterval(){
        return conf.getInt("autoSyncInterval", 5);
    }

    public boolean isSendJoinMessage(){
        return conf.getBoolean("onJoinMCBansMessage", false);
    }
    public boolean isSendDetailPrevBans(){
        return conf.getBoolean("sendDetailPrevBansOnJoin", false);
    }
    public double getMinRep(){
        return conf.getDouble("minRep", 3.0D);
    }
    public int getCallBackInterval(){
        return conf.getInt("callBackInterval", 15);
    }
    /*
    public boolean isSendPreviousBans(){
        return conf.getBoolean("sendPreviousBans", true);
    }
    */
    public int getTimeoutInSec(){
        return conf.getInt("timeout", 10);
    }
    public boolean isFailsafe(){
        return conf.getBoolean("failsafe", false);
    }
}
