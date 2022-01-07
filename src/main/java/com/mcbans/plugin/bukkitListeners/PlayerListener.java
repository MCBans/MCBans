package com.mcbans.plugin.bukkitListeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.client.response.BanResponse;
import com.mcbans.domain.Ban;
import com.mcbans.plugin.permission.Perms;
import com.mcbans.plugin.request.DisconnectRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.ConfigurationManager;
import com.mcbans.plugin.I18n;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.util.Util;

import static com.mcbans.plugin.I18n.localize;

public class PlayerListener implements Listener {
  private final MCBans plugin;
  private final ActionLog log;
  private final ConfigurationManager config;
  public static Cache<String, BanResponse> cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterWrite(15, TimeUnit.SECONDS)
    .build();

  public PlayerListener(final MCBans plugin) {
    this.plugin = plugin;
    this.log = plugin.getLog();
    this.config = plugin.getConfigs();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAsyncPlayerPreLoginEvent(final AsyncPlayerPreLoginEvent event) {
    try {
      BanResponse banResponse = cache.getIfPresent(event.getUniqueId().toString().toLowerCase());
      if (banResponse == null) {
        Client client = ConnectionPool.getConnection(config.getApiKey());

        // get player information
        banResponse = client.banStatusByPlayerUUID(event.getUniqueId().toString().toLowerCase(), event.getAddress().getHostAddress(), true);
        ConnectionPool.release(client);

        if (banResponse == null) {
          if (config.isFailsafe()) {
            log.warning("Null response! Kicked player: " + event.getName());
            event.disallow(Result.KICK_BANNED, localize("unavailable"));
          } else {
            log.warning("Null response! Check passed player: " + event.getName());
          }
          return;
        }
        cache.put(event.getName().toLowerCase(), banResponse);
      } else {
        plugin.debug("Retrieved from cache");
      }

      plugin.debug("Response: " + banResponse);
      rejectionHandler(banResponse, event); // handle all ban conditions

    } catch (SocketTimeoutException ex) {
      log.warning("Cannot connect to the MCBans API server: timeout");
      if (config.isFailsafe()) {
        event.disallow(Result.KICK_BANNED, localize("unavailable"));
      }
    } catch (IOException ex) {
      log.warning("Cannot connect to the MCBans API server!");
      if (config.isDebug()) ex.printStackTrace();

      if (config.isFailsafe()) {
        event.disallow(Result.KICK_BANNED, localize("unavailable"));
      }
    } catch (Exception ex) {
      log.warning("An error occurred in AsyncPlayerPreLoginEvent. Please report this!");
      ex.printStackTrace();

      if (config.isFailsafe()) {
        log.warning("Internal exception! Kicked player: " + event.getName());
        event.disallow(Result.KICK_BANNED, localize("unavailable"));
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    if (!event.getPlayer().getServer().getOnlineMode()) {
      (new Thread(new HandleConnectionData(event))).start(); // push to another thread to process the connection for bungeecord, does not save history
    } else {
      final Player player = event.getPlayer();
      final BanResponse banResponse = plugin.playerCache.remove(player.getName());
      if (banResponse == null) return;
      if (banResponse.getBans().size() > 0) {
        Util.message(player, ChatColor.RED + localize("bansOnRecord"));
        Perms.VIEW_BANS.message(ChatColor.RED + localize("previousBans", I18n.PLAYER, player.getName()));
        if (!Perms.HIDE_VIEW.has(player)) {
          if (config.isSendDetailPrevBans()) {
            banResponse.getBans().forEach(ban ->
              Perms.VIEW_BANS.message(localize("banInformation", I18n.ADMIN, ban.getAdminName(), I18n.REASON, ban.getReason(), I18n.SERVER, ban.getServerAddress()))
            );
          }
        }
      }
      /*if (pcache.containsKey("d")) {
        Util.message(player, ChatColor.RED + localize("disputes", I18n.COUNT, pcache.get("d")));
      }*/
      /*if (pcache.containsKey("pn")) {
        StringBuilder plist = new StringBuilder();
        for (String name : pcache.get("pn").split(",")) {
          plist.append(plist.length() == 0 ? "" : ", ").append(name);
        }
        Perms.VIEW_PREVIOUS.message(ChatColor.RED + localize("previousNames", I18n.PLAYER, player.getName(), I18n.PLAYERS, plist.toString()));
      }*/
      /*if (pcache.containsKey("dnsbl")) {
        StringBuilder proxlist = new StringBuilder();
        for (String name : pcache.get("dnsbl").split(",")) {
          String from = name.split("$")[0];
          String reason = name.split("$")[1];
          proxlist.append(proxlist.length() == 0 ? "" : ", ").append("[ ").append(from).append(" { ").append(reason).append(" } ]");
        }
        Perms.VIEW_PROXY.message(ChatColor.RED + localize("proxyDetected", I18n.PLAYER, player.getName(), I18n.REASON, proxlist.toString()));
      }
      if (pcache.containsKey("a")) {
        if (!Perms.HIDE_VIEW.has(player))
          Perms.VIEW_ALTS.message(ChatColor.DARK_PURPLE + localize("altAccounts", I18n.PLAYER, player.getName(), I18n.ALTS, pcache.get("al")));
      }*/
      if (banResponse.isMCBansStaff()) {
        //Util.broadcastMessage(ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
        // notify to console, mcbans.view.staff, mcbans.admin, mcbans.ban.global players
        Util.message(Bukkit.getConsoleSender(), localize("isMCBansMod", I18n.PLAYER, player.getName()));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
          Set<Player> players = Perms.VIEW_STAFF.getPlayers();
          players.addAll(Perms.ADMIN.getPlayers());
          players.addAll(Perms.BAN_GLOBAL.getPlayers());
          for (final Player p : players) {
            if (p.canSee(player)) { // check joined player cansee
              Util.message(p, localize("isMCBansMod", I18n.PLAYER, player.getName()));
            }
          }
        }, 1L);

        // send information to mcbans staff
        Set<String> admins = new HashSet<String>();
        for (Player p : Perms.ADMIN.getPlayers()) {
          admins.add(p.getName());
        }
        Util.message(player, ChatColor.AQUA + "You are an MCBans staff member. (ver " + plugin.getDescription().getVersion() + ")");
        Util.message(player, ChatColor.AQUA + "Online Admins: " + ((admins.size() > 0) ? Util.join(admins, ", ") : ChatColor.GRAY + "(none)"));

        // add online mcbans staff list array
        plugin.mcbStaff.add(player.getName());
      }
      if (config.isSendJoinMessage()) {
        Util.message(player, ChatColor.RED + "This server is secured by MCBans.");
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    // send disconnect request
    (new Thread(new DisconnectRequest(plugin, event.getPlayer().getName()))).start();

    if (plugin.mcbStaff.contains(event.getPlayer().getName())) {
      plugin.mcbStaff.remove(event.getPlayer().getName());
    }
  }

  private class HandleConnectionData implements Runnable {
    private PlayerJoinEvent event = null;

    public HandleConnectionData(PlayerJoinEvent event) {
      this.event = event;
    }

    @SuppressWarnings("unused")
    @Override
    public void run() {
      BanResponse response = null;
      try {

        Client client = ConnectionPool.getConnection(config.getApiKey()); // get connection

        // get player information
        response = client.banStatusByPlayerUUID(event.getPlayer().getUniqueId().toString().toLowerCase(), null, false);
        ConnectionPool.release(client);

        plugin.debug("Response: " + response);
      } catch (SocketTimeoutException ex) {
        log.warning("Cannot connect to the MCBans API server: timeout");
      } catch (IOException ex) {
        log.warning("Cannot connect to the MCBans API server!");
        if (config.isDebug()) ex.printStackTrace();
      } catch (Exception ex) {
        log.warning("An error occurred in AsyncPlayerPreLoginEvent. Please report this to an MCBans developer.");
        ex.printStackTrace();
      }
      final Player player = event.getPlayer();
      if (response == null) return;
      if (response.getBans().size()>0) {
        Util.message(player, ChatColor.RED + localize("bansOnRecord"));
        Perms.VIEW_BANS.message(ChatColor.RED + localize("previousBans", I18n.PLAYER, player.getName()));
        if (!Perms.HIDE_VIEW.has(player)) {
          if (config.isSendDetailPrevBans()) {
            response.getBans().forEach(ban ->
              Perms.VIEW_BANS.message(localize("banInformation", I18n.ADMIN, ban.getAdminName(), I18n.REASON, ban.getReason(), I18n.SERVER, ban.getServerAddress()))
            );
          }
        }
      }
      /*if (pcache.containsKey("d")) {
        Util.message(player, ChatColor.RED + localize("disputes", I18n.COUNT, pcache.get("d")));
      }
      if (pcache.containsKey("pn")) {
        StringBuilder plist = new StringBuilder();
        for (String name : pcache.get("pn").split(",")) {
          plist.append(plist.length() == 0 ? "" : ", ").append(name);
        }
        Perms.VIEW_PREVIOUS.message(ChatColor.RED + localize("previousNames", I18n.PLAYER, player.getName(), I18n.PLAYERS, plist.toString()));
      }*/
      /*if (pcache.containsKey("dnsbl")) {
        StringBuilder proxlist = new StringBuilder();
        for (String name : pcache.get("dnsbl").split(",")) {
          String from = name.split("$")[0];
          String reason = name.split("$")[1];
          proxlist.append(proxlist.length() == 0 ? "" : ", ").append("[ ").append(from).append(" { ").append(reason).append(" } ]");
        }
        Perms.VIEW_PROXY.message(ChatColor.RED + localize("proxyDetected", I18n.PLAYER, player.getName(), I18n.REASON, proxlist.toString()));
      }
      if (pcache.containsKey("a")) {
        if (!Perms.HIDE_VIEW.has(player))
          Perms.VIEW_ALTS.message(ChatColor.DARK_PURPLE + localize("altAccounts", I18n.PLAYER, player.getName(), I18n.ALTS, pcache.get("al")));
      }*/
      if (response.isMCBansStaff()) {
        //Util.broadcastMessage(ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
        // notify to console, mcbans.view.staff, mcbans.admin, mcbans.ban.global players
        Util.message(Bukkit.getConsoleSender(), localize("isMCBansMod", I18n.PLAYER, player.getName()));

        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
          @Override
          public void run() {
            Set<Player> players = Perms.VIEW_STAFF.getPlayers();
            players.addAll(Perms.ADMIN.getPlayers());
            players.addAll(Perms.BAN_GLOBAL.getPlayers());
            for (final Player p : players) {
              if (p.canSee(player)) { // check joined player cansee
                Util.message(p, localize("isMCBansMod", I18n.PLAYER, player.getName()));
              }
            }
          }
        }, 1L);

        // send information to mcbans staff
        Set<String> admins = Perms.ADMIN.getPlayers().stream().map(p -> p.getName()).collect(Collectors.toSet());
        Util.message(player, localize("mcbansStaffVersion", I18n.VERSION, plugin.getDescription().getVersion()));
        Util.message(player, localize("mcbansGiveAdminList", I18n.ADMINS, ((admins.size() > 0) ? Util.join(admins, ", ") : ChatColor.GRAY + "(none)")));

        // add online mcbans staff list array
        plugin.mcbStaff.add(player.getName());
      }
      if (config.isSendJoinMessage()) {
        Util.message(player, localize("mcbansServer"));
      }
    }

  }

  public void rejectionHandler(BanResponse response, AsyncPlayerPreLoginEvent event) {
    // check banned
    if (response.getBan() != null) {
      event.disallow(Result.KICK_BANNED, localize("banReturnMessage", I18n.REASON, response.getBan().getReason(), I18n.ADMIN, response.getBan().getAdminName(), I18n.BANID, response.getBan().getId(), I18n.TYPE, response.getBan().getType()));
      return;

      // check reputation
    } else if (response.getReputation() < config.getMinRep()) {
      event.disallow(Result.KICK_BANNED, localize("underMinRep"));
      return;

      // check alternate accounts ( Disabled for now, pending rebuild API side )
//    }else if (config.isEnableMaxAlts() && config.getMaxAlts() < Integer.valueOf(s[3]) && !Perms.EXEMPT_MAXALTS.has(event.getName())) {
//      event.disallow(Result.KICK_BANNED, localize("overMaxAlts"));
//      return;

      // check passed, put data to playerCache
    } else {
      plugin.playerCache.put(event.getName(), response);
    }
    plugin.debug(event.getName() + " authenticated with " + response.getReputation() + " rep");
  }
}