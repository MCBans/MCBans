package com.mcbans.firestar.mcbans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.h31ix.anticheat.Anticheat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.mcbans.firestar.mcbans.bukkitListeners.PlayerListener;
import com.mcbans.firestar.mcbans.callBacks.BanSync;
import com.mcbans.firestar.mcbans.callBacks.MainCallBack;
import com.mcbans.firestar.mcbans.callBacks.serverChoose;
import com.mcbans.firestar.mcbans.commands.BaseCommand;
import com.mcbans.firestar.mcbans.commands.*;
import com.mcbans.firestar.mcbans.log.ActionLog;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.log.Logger;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.rollback.RollbackHandler;

import fr.neatmonster.nocheatplus.NoCheatPlus;

public class BukkitInterface extends JavaPlugin {
    private static BukkitInterface instance;

    private MCBansCommandHandler commandHandler;
    private PlayerListener bukkitPlayer = new PlayerListener(this);
    public int taskID = 0;
    public HashMap<String, Integer> connectionData = new HashMap<String, Integer>();
    public HashMap<String, HashMap<String, String>> playerCache = new HashMap<String, HashMap<String, String>>();
    public HashMap<String, Long> resetTime = new HashMap<String, Long>();
    public Settings Settings;
    public long last_req = 0;
    public long timeRecieved = 0;
    public Language Language = null;
    public Thread callbackThread = null;
    public Thread syncBan = null;
    public boolean syncRunning = false;
    public long lastID = 0;
    public ActionLog actionLog = null;
    public long lastCallBack = 0;
    public long lastSync = 0;
    public boolean notSelectedServer = true;
    public String apiServers = "api01.cluster.mcbans.com,api02.cluster.mcbans.com,api03.cluster.mcbans.com,api.mcbans.com";
    public String apiServer = "";
    private String apiKey = "";
    public Logger logger = new Logger(this);
    private RollbackHandler rbHandler = null;
    private boolean ncpEnabled = false;
    private boolean acEnabled = false;

    @Override
    public void onDisable() {
        if (callbackThread != null) {
            if (callbackThread.isAlive()) {
                callbackThread.interrupt();
            }
        }
        if (syncBan != null) {
            if (syncBan.isAlive()) {
                syncBan.interrupt();
            }
        }
        log(LogLevels.INFO, "MCBans Disabled");
    }

    @Override
    public void onEnable() {
        instance = this;
        PluginManager pm = getServer().getPluginManager();

        // check online-mode
        if (!this.getServer().getOnlineMode()) {
            logger.log(LogLevels.FATAL, "MCBans: Your server is not in online mode!");
            pm.disablePlugin(pluginInterface("mcbans"));
            return;
        }


        pm.registerEvents(bukkitPlayer, this);

        // load configuration
        Settings = new Settings();
        if (Settings.exists) {
            pm.disablePlugin(pluginInterface("mcbans"));
            return;
        }
        this.apiKey = Settings.getString("apiKey");

        // load language
        String language;
        language = Settings.getString("language");
        log(LogLevels.INFO, "Loading language file: " + language);
        Language = new Language(this);

        // Setup logging
        if (Settings.getBoolean("logEnable")) {
            log(LogLevels.INFO, "Starting to save to log file!");
            actionLog = new ActionLog(this, Settings.getString("logFile"));
            actionLog.write("MCBans Log File Started");
        } else {
            log(LogLevels.INFO, "Log file disabled!");
        }

        // setup permissions
        //Permissions = new BukkitPermissions(Settings, this);
        Perms.setupPermissionHandler();

        // regist commands
        commandHandler = new MCBansCommandHandler(this, Settings);
        registerCommands();

        MainCallBack thisThread = new MainCallBack(this);
        callbackThread = new Thread(thisThread);
        callbackThread.start();

        // ban sync
        BanSync syncBanRunner = new BanSync(this);
        syncBan = new Thread(syncBanRunner);
        syncBan.start();

        serverChoose serverChooser = new serverChoose(this);
        (new Thread(serverChooser)).start();

        // rollback handler
        rbHandler = new RollbackHandler(this);
        rbHandler.setupHandler();

        // hookup integration plugin
        checkPlugin(true);
        if (ncpEnabled) log(LogLevels.INFO, "NoCheatPlus plugin found! Enabled this integration!");
        if (acEnabled) log(LogLevels.INFO, "AntiCheat plugin found! Enabled this integration!");

        log(LogLevels.INFO, "Started up successfully!");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
        return commandHandler.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args){
        return commandHandler.onTabComplete(sender, command, alias, args);
    }

    private void registerCommands(){
        List<BaseCommand> cmds = new ArrayList<BaseCommand>();
        // Banning Commands
        cmds.add(new CommandBan());
        cmds.add(new CommandGban());
        cmds.add(new CommandTempban());
        cmds.add(new CommandRban());

        // Other action commands
        cmds.add(new CommandUnban());
        cmds.add(new CommandKick());

        // Other commands
        cmds.add(new CommandLookup());
        cmds.add(new CommandMcbans());

        for (final BaseCommand cmd : cmds){
            commandHandler.registerCommand(cmd);
        }
    }

    public void checkPlugin(boolean startup){
        // Check NoCheatPlus
        Plugin checkNCP = getServer().getPluginManager().getPlugin("NoCheatPlus");
        this.ncpEnabled = (checkNCP != null && checkNCP instanceof NoCheatPlus);
        // Check AntiCheat
        Plugin checkAC = getServer().getPluginManager().getPlugin("AntiCheat");
        this.acEnabled = (checkAC != null && checkAC instanceof Anticheat);

        if (!startup){
            if (ncpEnabled) ncpEnabled = (checkNCP.isEnabled());
            if (acEnabled) acEnabled = (checkAC.isEnabled());
        }
    }

    public void log(String message) {
        log(LogLevels.NONE, message);
    }

    public void log(LogLevels type, String message) {
        if (actionLog != null) {
            actionLog.write(message);
        }
        logger.log(type, message);
    }

    public void broadcastAll(String msg) {
        for (Player player : this.getServer().getOnlinePlayers()) {
            player.sendMessage(Settings.getPrefix() + " " + msg);
        }
    }

    public void broadcastPlayer(final String playerName, String msg) {
        final Player target = this.getServer().getPlayer(playerName);
        if (target != null) {
            target.sendMessage(Settings.getPrefix() + " " + msg);
        } else {
            System.out.print(Settings.getPrefix() + " " + msg);
        }
    }

    public void broadcastPlayer(final Player target, String msg) {
        if (target != null && msg != null){
            target.sendMessage(Settings.getPrefix() + " " + msg);
        }
    }

    public void broadcastPlayer(final CommandSender target, String msg) {
        if (target != null && msg != null){
            target.sendMessage(Settings.getPrefix() + " " + msg);
        }
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public boolean isEnabledNCP(){
        return this.ncpEnabled;
    }

    public boolean isEnabledAC(){
        return this.acEnabled;
    }

    public RollbackHandler getRbHandler(){
        return this.rbHandler;
    }

    public MCBansCommandHandler getCmdHandler(){
        return this.commandHandler;
    }

    public Plugin pluginInterface(String pluginName) {
        return this.getServer().getPluginManager().getPlugin(pluginName);
    }

    public static BukkitInterface getInstance(){
        return instance;
    }
}
