package com.mcbans.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.mcbans.banlist.OfflineBanList;
import com.mcbans.client.response.BanResponse;
import com.mcbans.plugin.actions.PendingActions;
import com.mcbans.plugin.bukkitListeners.PlayerListener;
import com.mcbans.plugin.callBacks.BanSync;
import com.mcbans.plugin.callBacks.MainCallBack;
import com.mcbans.plugin.callBacks.ServerChoose;
import com.mcbans.plugin.permission.Perms;
import com.mcbans.plugin.rollback.RollbackHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.mcbans.plugin.api.MCBansAPI;
import com.mcbans.plugin.commands.BaseCommand;
import com.mcbans.plugin.commands.CommandAltlookup;
import com.mcbans.plugin.commands.CommandBan;
import com.mcbans.plugin.commands.CommandBanip;
import com.mcbans.plugin.commands.CommandBanlookup;
import com.mcbans.plugin.commands.CommandGlobalban;
import com.mcbans.plugin.commands.CommandKick;
import com.mcbans.plugin.commands.CommandLookup;
import com.mcbans.plugin.commands.CommandMCBansSettings;
import com.mcbans.plugin.commands.CommandMCBans;
import com.mcbans.plugin.commands.CommandPrevious;
import com.mcbans.plugin.commands.CommandRban;
import com.mcbans.plugin.commands.CommandTempban;
import com.mcbans.plugin.commands.CommandUnban;
import com.mcbans.plugin.commands.MCBansCommandHandler;

public class MCBans extends JavaPlugin {
  public final String apiRequestSuffix = "5.2.0";
  private static MCBans instance;

  private OfflineBanList offlineBanList;
  public int taskID = 0;
  public HashMap<String, Integer> connectionData = new HashMap<String, Integer>();
  public HashMap<String, BanResponse> playerCache = new HashMap<>();
  public HashMap<String, Long> resetTime = new HashMap<String, Long>();
  public Properties lastSyncs = new Properties();
  public ArrayList<String> mcbStaff = new ArrayList<String>();
  public long last_req = 0;
  public long timeRecieved = 0;
  public Thread callbackThread = null;
  public BanSync bansync = null;
  public Thread syncBan = null;
  public PendingActions pendingActions = null;
  public long lastID = -1;
  public File syncIni = null;
  public long lastSync = 0;
  public String lastType = "";
  public boolean syncRunning = false;
  public long lastCallBack = 0;
  public long lastPendingActions = 0;
  public static boolean AnnounceAll = false;
  public static boolean encryptAPI = false;
  public String apiServer = null;

  private ActionLog log = null;
  private RollbackHandler rbHandler = null;
  private boolean ncpEnabled = false;
  private boolean acEnabled = false;
  private ConfigurationManager config;
  private MCBansCommandHandler commandHandler;


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

    getServer().getScheduler().cancelTasks(this);
    instance = null;

    final PluginDescriptionFile pdfFile = this.getDescription();
    log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
  }

  @Override
  public void onEnable() {

    try {
      offlineBanList = new OfflineBanList(this);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    instance = this;
    PluginManager pm = getServer().getPluginManager();
    log = new ActionLog(this); // setup logger

    // check online-mode, Do NOT remove this check!
        /*if (!this.getServer().getOnlineMode()) {
            log.severe("This server is not in online mode!");
            pm.disablePlugin(this);
            return;
        }*/
    //load sync configuration
    syncIni = new File(this.getDataFolder(), "sync.ini");
    if (syncIni.exists()) {
      try {
        lastSyncs.load(new FileInputStream(syncIni));
        lastID = Long.valueOf(lastSyncs.getProperty("lastId"));
        lastType = lastSyncs.getProperty("lastType");
      } catch (Exception e) {
      }
    } else {
      lastType = "bans";
      lastID = -1;
    }

    // load configuration
    config = new ConfigurationManager(this);
    try {
      config.loadConfig(true);
    } catch (Exception ex) {
      log.warning("An error occurred while trying to load the config file.");
      ex.printStackTrace();
    }
    if (!pm.isPluginEnabled(this)) {
      return;
    }

    // load language
    log.info("Loading language file: " + config.getLanguage());
    I18n.init(config.getLanguage());

    encryptAPI = config.isEncryption();

    pm.registerEvents(new PlayerListener(this), this);

    // setup permissions
    Perms.setupPermissionHandler();

    // regist commands
    commandHandler = new MCBansCommandHandler(this);
    registerCommands();

    MainCallBack thisThread = new MainCallBack(this);
    thisThread.start();

    // ban sync
    bansync = new BanSync(this);
    bansync.start();

    pendingActions = new PendingActions(this);
    pendingActions.start();

    ServerChoose serverChooser = new ServerChoose(this);
    (new Thread(serverChooser)).start();

    // rollback handler
    rbHandler = new RollbackHandler(this);
    rbHandler.setupHandler();

    // hookup integration plugin
    //checkPlugin(true);
    //if (ncpEnabled) log.info("NoCheatPlus plugin found! Enabled this integration!");
    //if (acEnabled) log.info("AntiCheat plugin found! Enabled this integration!");

    final PluginDescriptionFile pdfFile = this.getDescription();
    log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
  }

  @Override
  public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    return commandHandler.onCommand(sender, command, label, args);
  }

  @Override
  public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
    return commandHandler.onTabComplete(sender, command, alias, args);
  }

  private void registerCommands() {
    List<BaseCommand> cmds = new ArrayList<BaseCommand>();
    // Banning Commands
    cmds.add(new CommandBan());
    cmds.add(new CommandGlobalban());
    cmds.add(new CommandTempban());
    cmds.add(new CommandRban());

    // IP Banning Commands
    cmds.add(new CommandBanip());

    // Other action commands
    cmds.add(new CommandUnban());
    cmds.add(new CommandKick());

    // Other commands
    cmds.add(new CommandLookup());
    cmds.add(new CommandBanlookup());
    cmds.add(new CommandAltlookup());
    cmds.add(new CommandMCBans());
    cmds.add(new CommandPrevious());

    cmds.add(new CommandMCBansSettings());

    for (final BaseCommand cmd : cmds) {
      commandHandler.registerCommand(cmd);
    }
  }

  public void debug(final String message) {
    if (getConfigs().isDebug()) {
      getLog().info(message);
    }
  }

    /*public void checkPlugin(final boolean startup){
        // Check NoCheatPlus
        //Plugin checkNCP = getServer().getPluginManager().getPlugin("NoCheatPlus");
        this.ncpEnabled = (checkNCP != null && checkNCP instanceof NoCheatPlus);
        // Check AntiCheat
        //Plugin checkAC = getServer().getPluginManager().getPlugin("AntiCheat");
        this.acEnabled = (checkAC != null && checkAC instanceof Anticheat);

        if (!startup){
            if (ncpEnabled) ncpEnabled = (checkNCP.isEnabled());
            if (acEnabled) acEnabled = (checkAC.isEnabled());
        }
    }*/

  public boolean isEnabledNCP() {
    return this.ncpEnabled;
  }

  public boolean isEnabledAC() {
    return this.acEnabled;
  }

  public RollbackHandler getRbHandler() {
    return this.rbHandler;
  }

  public MCBansAPI getAPI(final Plugin plugin) {
    return MCBansAPI.getHandle(this, plugin);
  }


  public static UUID fromString(String uuid){
    return UUID.fromString(uuid.replaceAll("(?ism)([a-z0-9]{8})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{12})", "$1-$2-$3-$4-$5"));
  }

  public static Player getPlayer(Plugin plugin, UUID uuid) {
    return plugin.getServer().getPlayer(uuid);
  }

  public static Player getPlayer(Plugin plugin, String target) {
    return plugin.getServer().getPlayerExact(target);
  }

  /*public void act(String act, String uuid){
    uuid = uuid.replaceAll("(?ism)([a-z0-9]{8})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{12})", "$1-$2-$3-$4-$5");
    if(uuid.matches("([a-z0-9]{8})-([a-z0-9]{4})-([a-z0-9]{4})-([a-z0-9]{4})-([a-z0-9]{12})")){
      OfflinePlayer d = getServer().getOfflinePlayer(UUID.fromString(uuid));
      if (d != null){
        if(d.isBanned()){
              if(act.equals("unban")){
                  d.setBanned(false);
              }
          }else{
              if(act.equals("ban")){
                  d.setBanned(true);
              }
          }
      }
    }
  }*/
  public ConfigurationManager getConfigs() {
    return this.config;
  }

  public ActionLog getLog() {
    return this.log;
  }

  public static String getPrefix() {
    return instance.config.getPrefix();
  }

  public static MCBans getInstance() {
    return instance;
  }

  public OfflineBanList getOfflineBanList() {
    return offlineBanList;
  }
}
