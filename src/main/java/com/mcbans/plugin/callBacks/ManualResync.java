package com.mcbans.plugin.callBacks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.mcbans.client.BadApiKeyException;
import com.mcbans.plugin.request.JsonHandler;
import com.mcbans.utils.TooLargeException;
import org.bukkit.BanList;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.org.json.JSONException;
import com.mcbans.plugin.org.json.JSONObject;
import com.mcbans.plugin.util.Util;
import org.bukkit.scheduler.BukkitRunnable;

public class ManualResync implements Runnable {
  private final MCBans plugin;
  private final String commandSend;

  public ManualResync(final MCBans plugin, final String sender) {
    this.plugin = plugin;
    this.commandSend = sender;
  }

  @Override
  public void run() {

    if (plugin.syncRunning) {
      // What you want to schedule goes here
      Util.message(commandSend, ChatColor.GREEN + "Sync is already running.");
      return;
    }
    plugin.lastID = -1;
    Util.message(commandSend, ChatColor.GREEN + "Sync started at: " + plugin.lastID);
    new Thread(() -> {
      try {
        new BanSync(plugin).startSync(new BanSync.Responder() {
          @Override
          public void ack() {
            Util.message(commandSend, ChatColor.GREEN + "Sync is complete. lastBanId: " + plugin.lastID);
          }

          @Override
          public void partial(long total, long current) {
            Util.message(commandSend, ChatColor.YELLOW + "Current Percentage: " + Math.round((Long.valueOf(current).doubleValue() / Long.valueOf(total)) * 100) + "%");
          }

          @Override
          public void error() {
            Util.message(commandSend, ChatColor.RED + "Error syncing bans.");
          }
        });
      } catch (IOException e) {
        if (plugin.getConfigs().isDebug())
          e.printStackTrace();
      } catch (BadApiKeyException e) {
        if (plugin.getConfigs().isDebug())
          e.printStackTrace();
      } catch (InterruptedException e) {
        if (plugin.getConfigs().isDebug())
          e.printStackTrace();
      } catch (TooLargeException e) {
        if (plugin.getConfigs().isDebug())
          e.printStackTrace();
      }
    }).start();
  }
}
