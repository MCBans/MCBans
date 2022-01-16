package com.mcbans.plugin.callBacks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import com.mcbans.client.BadApiKeyException;
import com.mcbans.client.BanSyncClient;
import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.TooLargeException;
import de.diddiz.lib.org.slf4j.event.Level;
import org.bukkit.BanList;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.org.json.JSONException;
import org.bukkit.scheduler.BukkitRunnable;

public class BanSync {
  private final MCBans plugin;

  public BanSync(MCBans plugin) {
    this.plugin = plugin;
  }

  public void goRequest() {
    this.startSync();
  }

  public void startSync() {
    if (plugin.syncRunning) {
      return;
    }
    plugin.syncRunning = true;
    try {
      Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
      try {
        AtomicLong lastBanId = new AtomicLong(plugin.lastID);
        BanSyncClient.cast(client).getBanSync(plugin.lastID, new Client.ResponseHandler(){
          @Override
          public void bans(List<Ban> bans) {
            bans.forEach(ban->{
              new BukkitRunnable() {
                @Override
                public void run() {
                  if(ban.getType().equals("temp")){ // ignore temp bans

                  }else {
                    plugin.getServer().getBanList(BanList.Type.NAME).addBan(ban.getPlayer().getName(), ban.getReason(), null, ban.getId() + ":mcbans:" + ((ban.getAdmin() != null) ? ban.getAdmin().getName() : "console"));
                  }
                }
              }.runTaskLater(plugin, 0);
              lastBanId.set((ban.getId()> lastBanId.get())?ban.getId():lastBanId.get());
            });
          }
        });
        plugin.debug("Received bans from: " + plugin.lastID + " to: " + lastBanId.get());
        plugin.lastID = lastBanId.get();
        plugin.lastSync = System.currentTimeMillis() / 1000;
        save();
      } catch (NullPointerException e) {
        if (plugin.getConfigs().isDebug()) {
          e.printStackTrace();
        }
      } catch (ClassNotFoundException e) {
        if (plugin.getConfigs().isDebug()) {
          e.printStackTrace();
        }
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
  public void start(){
    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        if (plugin.getConfigs().isEnableAutoSync()) {
          startSync();
          plugin.getLog().info("Completed ban sync.");
        }
      }
    }, 0, 1000*60*5); // repeat every 5 minutes.
  }

  public void save() {
    plugin.lastSyncs.setProperty("lastId", String.valueOf(plugin.lastID));
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
