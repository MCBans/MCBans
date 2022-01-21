package com.mcbans.plugin.callBacks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.mcbans.client.BadApiKeyException;
import com.mcbans.client.BanSyncClient;
import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.TooLargeException;
import de.diddiz.lib.org.slf4j.event.Level;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.BanList;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.org.json.JSONException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.FileUtil;

public class BanSync {
  private final MCBans plugin;

  public static class Responder {
    void ack() {
    }

    void error() {
    }

    void partial(long total, long current) {
    }
  }

  public BanSync(MCBans plugin) {
    this.plugin = plugin;
  }

  public void goRequest() {
    this.startSync();
  }

  public void startSync() {
    startSync(null);
  }

  public void startSync(Responder responder) {
    if (plugin.syncRunning) {
      return;
    }

    boolean resync = plugin.lastID == 0;
    if(plugin.lastSyncs.getProperty("v2", "false").equals("false")){
      resync = true;
    }
    plugin.syncRunning = true;
    try {
      Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
      try {
        OutputStream out = null;
        File file = null;
        List<Ban> bansWithNoUUID = null;
        if (resync) {
          file = new File("banned-players.json_downloading");
          file.createNewFile();
          bansWithNoUUID = new LinkedList<>();
          out = new FileOutputStream(file);
          out.write("[".getBytes(StandardCharsets.UTF_8));
        }
        AtomicLong lastBanId = new AtomicLong(plugin.lastID);
        AtomicLong x = new AtomicLong(0);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss Z");
        List<Ban> finalBansWithNoUUID = bansWithNoUUID;
        OutputStream finalOut = out;
        boolean finalResync = resync;
        BanSyncClient.cast(client).getBanSync(plugin.lastID, new Client.ResponseHandler() {
          @Override
          public void bans(List<Ban> bans) {
            bans.stream().filter(
              ban -> !ban.getType().equals("temp")
            ).forEach(
              ban -> {
                if (ban.getId() > lastBanId.get()) lastBanId.set(ban.getId());
                if (finalResync) {
                  try {
                    if (ban.getPlayer() != null && ban.getPlayer().getUuid() != null && ban.getPlayer().getUuid().length() == 32) {
                      finalOut.write((((x.get() == 0) ? "" : ",") + "{" +
                        "\"uuid\":\"" + MCBans.fromString(ban.getPlayer().getUuid()) + "\"" +
                        ",\"name\":\"" + ban.getPlayer().getName() + "\"" +
                        ",\"created\":\"" + dateFormat.format(ban.getDate()) + "\"" +
                        ",\"expires\":\"forever\"" +
                        ",\"source\":\"" + ban.getId() + ":mcbans:" + ((ban.getAdmin() != null) ? ban.getAdmin().getName() : "console") + "\"" +
                        ",\"reason\":" + new Gson().toJson(ban.getReason()) + " " +
                        "}").getBytes(StandardCharsets.UTF_8));
                    } else {
                      finalBansWithNoUUID.add(ban);
                    }
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                } else {
                  plugin.getServer().getBanList(BanList.Type.NAME).addBan(ban.getPlayer().getName(), ban.getReason(), null, ban.getId() + ":mcbans:" + ((ban.getAdmin() != null) ? ban.getAdmin().getName() : "console"));
                }
                x.incrementAndGet();
              }
            );
          }
          @Override
          public void partial(long total, long current) {
            if (responder != null) responder.partial(total, current);
          }
        });
        if(resync) {
          out.write("]".getBytes(StandardCharsets.UTF_8));
          out.close();
          File targetFile = new File("banned-players.json");
          targetFile.delete();
          file.renameTo(targetFile);
        }
        ConnectionPool.release(client);
        plugin.debug("Received bans from: " + plugin.lastID + " to: " + lastBanId.get());
        plugin.lastID = lastBanId.get();
        plugin.lastSync = System.currentTimeMillis() / 1000;
        if (responder != null) responder.ack();
        save();
        if(resync) {
          new BukkitRunnable(){
            @Override
            public void run() {
              Bukkit.getServer().reload(); // dirty reload
              finalBansWithNoUUID.forEach(ban->plugin.getServer().getBanList(BanList.Type.NAME).addBan(ban.getPlayer().getName(), ban.getReason(), null, ban.getId() + ":mcbans:" + ((ban.getAdmin() != null) ? ban.getAdmin().getName() : "console")));
            }
          }.runTaskLater(plugin, 30);
        }
      } catch (NullPointerException e) {
        if (plugin.getConfigs().isDebug()) {
          e.printStackTrace();
        }
        if (responder != null) responder.error();
      } catch (ClassNotFoundException e) {
        if (plugin.getConfigs().isDebug()) {
          e.printStackTrace();
        }
        if (responder != null) responder.error();
      }
    } catch (IOException ioException) {
      ioException.printStackTrace();
    } catch (BadApiKeyException badApiKeyException) {
      badApiKeyException.printStackTrace();
    } catch (InterruptedException interruptedException) {
      interruptedException.printStackTrace();
    } catch (TooLargeException tooLargeException) {
      tooLargeException.printStackTrace();
    } finally {
      plugin.syncRunning = false;
    }
  }

  public void start() {
    new Thread(() -> {
      new Timer().scheduleAtFixedRate(new TimerTask() {
        public void run() {
          if (plugin.getConfigs().isEnableAutoSync()) {
            startSync();
            plugin.getLog().info("Completed ban sync.");
          }
        }
      }, 0, 1000 * 60 * plugin.getConfigs().getSyncInterval()); // repeat every 5 minutes.
    }).start();
  }

  public void save() {
    plugin.lastSyncs.setProperty("lastId", String.valueOf(plugin.lastID));
    plugin.lastSyncs.setProperty("v2", "true");
    try {
      plugin.lastSyncs.store(new FileOutputStream(plugin.syncIni), "Syncing ban information.");
    } catch (FileNotFoundException e) {
      if (plugin.getConfigs().isDebug()) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      if (plugin.getConfigs().isDebug()) {
        e.printStackTrace();
      }
    }
  }
}
