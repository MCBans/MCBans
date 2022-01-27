package com.mcbans.plugin.request;

import static com.mcbans.plugin.I18n.localize;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.mcbans.banlist.BannedPlayer;
import com.mcbans.client.*;
import com.mcbans.client.response.BanResponse;
import com.mcbans.plugin.bukkitListeners.PlayerListener;
import com.mcbans.plugin.events.PlayerGlobalBanEvent;
import com.mcbans.plugin.events.PlayerLocalBanEvent;
import com.mcbans.plugin.events.PlayerTempBanEvent;
import com.mcbans.plugin.permission.Perms;
import com.mcbans.utils.IPTools;
import com.mcbans.utils.TimeTools;
import com.mcbans.utils.TooLargeException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.BanList;
import org.bukkit.entity.Player;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.I18n;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.events.PlayerBanEvent;
import com.mcbans.plugin.events.PlayerUnbanEvent;
import com.mcbans.plugin.events.PlayerUnbannedEvent;
import com.mcbans.plugin.org.json.JSONException;
import com.mcbans.plugin.org.json.JSONObject;
import com.mcbans.plugin.util.Util;

public class Ban {
  private final MCBans plugin;
  private final ActionLog log;

  private String playerName, playerIP, senderName, reason, action, duration, measure, badword, playerUUID, senderUUID = null;
  private boolean rollback = false;
  private JSONObject actionData = null;
  private HashMap<String, Integer> responses = new HashMap<String, Integer>();
  private int action_id;

  public Ban(MCBans plugin, String action, String playerName, String playerUUID, String playerIP, String senderName, String senderUUID,
             String reason, String duration, String measure, JSONObject actionData, boolean rollback) {
    this(plugin, action, playerName, playerIP, senderName, reason, duration,
      measure, actionData, rollback);
    this.playerUUID = playerUUID;
    this.senderUUID = senderUUID;
  }

  public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
             String measure, JSONObject actionData, boolean rollback) {
    this.plugin = plugin;
    this.log = plugin.getLog();

    this.playerName = playerName;
    this.playerIP = playerIP;
    this.senderName = senderName;
    this.reason = reason;
    this.rollback = rollback;
    this.duration = duration;
    this.measure = measure;
    this.action = action;
    this.actionData = (actionData != null) ? actionData : new JSONObject();
    responses.put("globalBan", 0);
    responses.put("localBan", 1);
    responses.put("tempBan", 2);
    responses.put("unBan", 3);
  }

  public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
             String measure) {
    this(plugin, action, playerName, playerIP, senderName, reason, duration, measure, null, false);
  }

  public void kickPlayer(String playerName, String playerUUID, final String kickReason) {
    Player targettmp = null;
    if (playerUUID != null) {
      targettmp = MCBans.getPlayer(plugin, playerUUID);
    } else {
      targettmp = MCBans.getPlayer(plugin, playerName);
    }
    final Player target = targettmp;
    if (target != null) {
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> target.kickPlayer(kickReason), 1);
    }
  }

  public void run() {
    try {

      if (responses.containsKey(action)) {
        action_id = responses.get(action);

        // Call BanEvent
        if (action_id != 3) {
          PlayerBanEvent banEvent = new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure);
          ;
          plugin.getServer().getPluginManager().callEvent(banEvent);
          if (banEvent.isCancelled()) {
            return;
          }
          senderName = banEvent.getSenderName();
          reason = banEvent.getReason();
          action_id = banEvent.getActionID();
          duration = banEvent.getDuration();
          measure = banEvent.getMeasure();
        }
        Player targettmp = null;
        if (playerUUID != null) {
          targettmp = MCBans.getPlayer(plugin, MCBans.fromString(playerUUID));
        } else {
          targettmp = MCBans.getPlayer(plugin, playerName);
        }
        /*if (targettmp != null && action_id != 3) {
          if (Perms.EXEMPT_BAN.has(targettmp)) {
            Util.message(senderName, ChatColor.RED + localize("banExemptPlayer", I18n.PLAYER, targettmp.getName()));
            return;
          }
        } else if (playerName != null && action_id != 3) {
          if (Perms.EXEMPT_BAN.has(playerName)) {
            Util.message(senderName, ChatColor.RED + localize("banExemptPlayer", I18n.PLAYER, playerName));
            return;
          }
        }*/
        switch (action_id) {
          case 0:
            globalBan();
            break;
          case 1:
            localBan();
            break;
          case 2:
            tempBan();
            break;
          case 3:
            unBan();
            break;
        }
      } else {
        err();
      }
    } catch (Exception e) {
      e.printStackTrace();
      err();
    }
  }

  public void unBan() throws IOException, BadApiKeyException, InterruptedException, TooLargeException {
    // Call PlayerUnbanEvent
    PlayerUnbanEvent unBanEvent = new PlayerUnbanEvent(playerName, playerUUID, senderName, senderUUID);
    plugin.getServer().getPluginManager().callEvent(unBanEvent);
    if (unBanEvent.isCancelled()) {
      return;
    }
    senderName = unBanEvent.getSenderName();

    new Thread(() -> {
      try {
        Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
        Client.ResponseHandler responder = new Client.ResponseHandler() {
          @Override
          public void err(String error) {
            Util.message(senderName, ChatColor.RED + error);
          }

          @Override
          public void ack() {
            Util.broadcastMessage(ChatColor.GREEN + localize("unBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName));
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().callEvent(new PlayerUnbannedEvent(playerName, playerUUID, senderName, senderUUID)), 1);
            log.info(senderName + " unbanned " + playerName + "!");
            return;
          }
        };
        if (!IPTools.validBanIP(playerName)) {
          UnbanClient.cast(client).unBan(playerName, playerUUID, responder); // unban player
        } else {
          UnBanIpClient.cast(client).unBanIp(playerName, responder); // unban IP
        }
        ConnectionPool.release(client);
        if (!IPTools.validIP(playerName)) {
          bukkitBan(false, null,null, false);
        } else if(IPTools.validIP(playerName)){
          Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getServer().unbanIP(playerName), 1);
        }
      } catch (IOException e) {
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. Defaulting to local unban.");
        // First, remove from bukkit banlist
        if (!IPTools.validBanIP(playerName)) {
          bukkitBan(false, null, null);
        } else if (IPTools.validIP(playerName)) {
          Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getServer().unbanIP(playerName), 1);
        }
        log.warning("Error occurred with local banning. Please report this to an MCBans developer.");
      } catch (BadApiKeyException | TooLargeException | InterruptedException | ClassNotFoundException e) {
        e.printStackTrace();
        log.info(senderName + " tried to unban " + playerName + "!");
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
      }
    }).start();
  }

  public void localBan() {
    // Call PlayerLocalBanEvent
    PlayerLocalBanEvent lBanEvent = new PlayerLocalBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason);
    plugin.getServer().getPluginManager().callEvent(lBanEvent);
    if (lBanEvent.isCancelled()) {
      return;
    }
    senderName = lBanEvent.getSenderName();
    reason = lBanEvent.getReason();

    new Thread(() -> {
      try {
        Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
        BanClient.cast(client).localBan(playerName, playerUUID, playerIP, senderUUID, reason, new Client.ResponseHandler() {
          @Override
          public void err(String error) {
            Util.message(senderName, ChatColor.RED + error);
          }

          @Override
          public void ack() {
            if (rollback) {
              plugin.getRbHandler().rollback(senderName, playerName);
            }
            kickPlayer(playerName, playerUUID, localize("localBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
            Util.broadcastMessage(ChatColor.GREEN + localize("localBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure)), 1);
            log.info(playerName + " has been banned with a local type ban [" + reason + "] [" + senderName + "]!");
          }
        });
        ConnectionPool.release(client);
      } catch (IOException e) {
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. Offline ban added!");
        // add to offline banlist
        bukkitBan(true, "local", null);
        log.warning("Error occurred with local banning. Please report this to an MCBans developer.");
      } catch (BadApiKeyException | TooLargeException | InterruptedException | ClassNotFoundException e) {
        e.printStackTrace();
        log.info(senderName + " tried to ban " + playerName + " with a local type ban [" + reason + "]!");
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
      }
    }).start();
  }

  public void globalBan() {
    // Call PlayerGlobalBanEvent
    PlayerGlobalBanEvent gBanEvent = new PlayerGlobalBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason);
    plugin.getServer().getPluginManager().callEvent(gBanEvent);
    if (gBanEvent.isCancelled()) {
      return;
    }
    senderName = gBanEvent.getSenderName();
    reason = gBanEvent.getReason();

    new Thread(() -> {
      try {
        Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
        BanClient.cast(client).globalBan(playerName, playerUUID, playerIP, senderUUID, reason, new Client.ResponseHandler() {
          @Override
          public void err(String error) {
            Util.message(senderName, ChatColor.RED + error);
          }

          @Override
          public void ack() {
            if (rollback) {
              plugin.getRbHandler().rollback(senderName, playerName);
            }
            kickPlayer(playerName, playerUUID, localize("globalBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
            Util.broadcastMessage(ChatColor.GREEN + localize("globalBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));

            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure)), 1);

            log.info(playerName + " has been banned with a global type ban [" + reason + "] [" + senderName + "]!");
          }
        });
        ConnectionPool.release(client);
      } catch (IOException e) {
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
        // add to offline banlist
        bukkitBan(true, "global", null);
        log.warning("Error occurred with local banning. Please report this to an MCBans developer.");
      } catch (BadApiKeyException | TooLargeException | InterruptedException | ClassNotFoundException e) {
        e.printStackTrace();
        log.info(senderName + " tried to ban " + playerName + " with a global type ban [" + reason + "]!");
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
      }
    }).start();
  }

  public void tempBan() {
    // Call PlayerTempBanEvent
    PlayerTempBanEvent tBanEvent = new PlayerTempBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, duration, measure);
    plugin.getServer().getPluginManager().callEvent(tBanEvent);
    if (tBanEvent.isCancelled()) {
      return;
    }
    senderName = tBanEvent.getSenderName();
    reason = tBanEvent.getReason();
    duration = tBanEvent.getDuration();
    measure = tBanEvent.getMeasure();

    new Thread(() -> {
      try {
        Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
        BanClient.cast(client).tempBan(playerName, playerUUID, playerIP, senderUUID, reason, duration + measure, new Client.ResponseHandler() {
          @Override
          public void err(String error) {
            Util.message(senderName, ChatColor.RED + error);
          }

          @Override
          public void ack() {
            kickPlayer(playerName, playerUUID, localize("tempBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
            Util.broadcastMessage(ChatColor.GREEN + localize("tempBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure)), 1);
            log.info(playerName + " has been banned with a temp type ban [" + reason + "] [" + senderName + "]!");
          }
        });
        ConnectionPool.release(client);
      } catch (IOException e) {
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
        bukkitBan(true, "temp", TimeTools.convertStringToDate(duration + "" + measure));
        log.warning("Error occurred with local banning. Please report this to an MCBans developer.");
      } catch (BadApiKeyException | TooLargeException | InterruptedException | ClassNotFoundException e) {
        e.printStackTrace();
        log.info(senderName + " tried to ban " + playerName + " with a temp type ban [" + reason + "]!");
        Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
      }
    }).start();
  }
  private void bukkitBan(final boolean flag, String type, Long expires){
    bukkitBan(flag, type, expires, true);
  }

  void addBukkitBanByUUID(String name, String uuid, String type, Long expires, boolean fallback){
    //plugin.getServer().getBanList(BanList.Type.NAME).addBan(target.getName(), reason, new Date(), senderName);
    plugin.getOfflineBanList().addBan(uuid, new BannedPlayer(
      type,
      name,
      uuid,
      reason,
      senderName,
      senderUUID,
      new Date(),
      expires
    ));
    try {
      plugin.getOfflineBanList().save();
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.kickPlayer(playerName, uuid, localize("localBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));

  }
  private void bukkitBan(final boolean flag, String type, Long expires, boolean fallback) {
    OfflinePlayer target = null;
    String uuid = null;
    String playerNameFound = null;
    if (playerName != null && playerName.length() >= 2 && playerName.length() <= 16) {
      target = plugin.getServer().getOfflinePlayer(playerName);
      playerNameFound = playerName;
    }
    if (playerName != null && playerName.length() == 32) {
      uuid = playerName;
    }else if (playerUUID != null && playerUUID.length() == 32) {
      uuid = playerUUID;
    }
    if (flag) {
      if (uuid==null && playerNameFound!=null && target.getUniqueId() == null) {
        uuid = Util.getStringUUID(playerNameFound);
      }
      if (!plugin.getOfflineBanList().isBanned(uuid)) {
        addBukkitBanByUUID(playerNameFound, uuid, type, expires, fallback);
      }
    } else {
      BannedPlayer bannedPlayer= null;
      if(uuid!=null) {
        bannedPlayer = plugin.getOfflineBanList().get(uuid);
      }else if(playerNameFound!=null){
        bannedPlayer = plugin.getOfflineBanList().getByPlayerName(playerNameFound);
      }
      if (bannedPlayer!=null && bannedPlayer.isBanned()) {
        if(fallback) {
          bannedPlayer.setBanned(false);
        }else{
          plugin.getOfflineBanList().remove(bannedPlayer.getPlayerUUID());
        }
        try {
          plugin.getOfflineBanList().save();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private Map<String, JSONObject> getProof() throws JSONException {
    HashMap<String, JSONObject> ret = new HashMap<String, JSONObject>();

    /* Hacked client */
    // No catch PatternSyntaxException. This exception thrown when compiling invalid regex.
    // In this case, regex is constant string. Next line is wrong if throw this. So should output full exception message.
    Pattern regex = Pattern.compile("(fly|hack|nodus|glitch|exploit|NC|cheat|nuker|x-ray|xray)");
    boolean foundMatch = regex.matcher(reason).find();

    if (foundMatch) {
      Player p = plugin.getServer().getPlayerExact(playerName);
      if (p != null) playerName = p.getName();
    }

    return ret;
  }

  private void err() {
    log.warning("\nError: MCBans caught an invalid action. Perhaps another plugin is using MCBans improperly?\n");
  }
}
