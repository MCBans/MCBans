package com.mcbans.plugin.callBacks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.mcbans.plugin.request.JsonHandler;
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
    plugin.lastID = 0;
    new BanSync(plugin).start();

    Util.message(commandSend, ChatColor.GREEN + "Sync is complete.");
  }
}
