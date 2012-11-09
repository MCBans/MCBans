package com.mcbans.firestar.mcbans.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

import com.mcbans.firestar.mcbans.MCBans;

public class ConfigurationManager {
    private final int latestVersion = 1;
    private final MCBans plugin;

    //private YamlConfiguration conf;
    private FileConfiguration conf;
    private File pluginDir;

    /**
     * Constructor
     */
    public ConfigurationManager(final MCBans plugin){
        this.plugin = plugin;
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
            System.out.print("[MCBans] config.yml is not found! Created default config.yml!");
        }

        plugin.reloadConfig();
        conf = plugin.getConfig();

        checkver(conf.getInt("ConfigVersion", 1));
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
                System.out.print("[MCBans] Copied old config.yml to "+destName+"!");
            }catch(Exception ex){
                System.out.print("[MCBans] Failed to copy old config.yml!");
            }

            // force copy config.yml and languages
            FileStructure.extractResource("/config.yml", pluginDir, true, false);
            //Language.extractLanguageFile(true);

            System.out.print("[MCBans] Copied existing configuration file and generate a new one!");
        }
    }

    /* ***** Begin Configuration Getters *********************** */
    public String getPrefix(){
        return conf.getString("prefix", "[mcbans]");
    }
    public String getApiKey(){
        return conf.getString("apiKey", "");
    }
    public String getLanguage(){
        return conf.getString("language", "en-us");
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
        return conf.getString("logFile", "plugins/mcbans/actions.log");
    }

    public boolean isEnableMaxAlts(){
        return conf.getBoolean("enableMaxAlts", false);
    }
    public int getMaxAlts(){
        return conf.getInt("maxAlts", 2);
    }

    public boolean isEnableRollbackOnBan(){
        return conf.getBoolean("rollbackOnBan", false);
    }
    public boolean isEnableRollbackTempBan(){
        return conf.getBoolean("enableTempBanRollback", false);
    }
    public String getAffectedWorlds(){
        return conf.getString("affectedWorlds", "world");
    }
    public int getBackDaysAgo(){
        return conf.getInt("backDaysAgo", 20);
    }

    public boolean isEnableSyncBans(){
        return conf.getBoolean("syncBans", true);
    }
    public int getSyncInterval(){
        return conf.getInt("syncInterval", 1);
    }

    public boolean isEnableJoinMessage(){
        return conf.getBoolean("onJoinMCBansMessage", false);
    }
    public int getMinRep(){
        return conf.getInt("minRep", 3);
    }
    public int getCallBackInterval(){
        return conf.getInt("callBackInterval", 15);
    }
    public boolean isEnableConnectGlobals(){
        return conf.getBoolean("onConnectGlobals", true);
    }
}
