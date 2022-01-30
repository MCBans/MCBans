package com.mcbans.plugin.callBacks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.mcbans.banlist.BannedPlayer;
import com.mcbans.client.*;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.TimeTools;
import com.mcbans.utils.TooLargeException;
import com.mcbans.plugin.MCBans;

public class BanSync {
  private final MCBans plugin;

  public static class Responder {
    public void ack() {
    }

    public void error() {
    }

    public void partial(long total, long current) {
    }
  }

  public BanSync(MCBans plugin) {
    this.plugin = plugin;
  }

  public void goRequest() throws IOException, BadApiKeyException, InterruptedException, TooLargeException {
    this.startSync();
  }

  public void startSync() throws IOException, BadApiKeyException, InterruptedException, TooLargeException {
    startSync(null);
  }

  void startSync(Responder responder) throws IOException, BadApiKeyException, InterruptedException, TooLargeException {
    if (plugin.syncRunning) {
      return;
    }
    Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());


    client.verifyConnection();
    UnbanClient uc = UnbanClient.cast(client);
    BanClient bc = BanClient.cast(client);

    // add bans to server when online
    List<BannedPlayer> unSyncedBans = plugin.getOfflineBanList().unSynced();
    if(unSyncedBans!=null) {
      unSyncedBans.forEach(banned -> {
        if (!banned.isBanned()) {
          try {
            uc.unBan(banned.getPlayerName(), banned.getPlayerUUID(), new Client.ResponseHandler() {
              @Override
              public void ack() {
                plugin.getOfflineBanList().remove(banned.getPlayerUUID());
              }
            });
          } catch (IOException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (ClassNotFoundException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (TooLargeException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          }
        } else {
          try {
            switch (banned.getType()) {
              case "global":
                bc.globalBan(banned.getPlayerName(), banned.getPlayerUUID(), null, banned.getAdminUUID(), banned.getReason(), new Client.ResponseHandler() {
                  @Override
                  public void ack() {
                    plugin.getOfflineBanList().remove(banned.getPlayerUUID());
                  }
                });
                break;
              case "local":
                bc.localBan(banned.getPlayerName(), banned.getPlayerUUID(), null, banned.getAdminUUID(), banned.getReason(), new Client.ResponseHandler() {
                  @Override
                  public void ack() {
                    plugin.getOfflineBanList().remove(banned.getPlayerUUID());
                  }
                });
                break;
              case "temp":
                long left = banned.getExpires() - new Date().getTime();
                bc.tempBan(banned.getPlayerName(), banned.getPlayerUUID(), null, banned.getAdminUUID(), banned.getReason(), TimeTools.countDownForBanSync(left), new Client.ResponseHandler() {
                  @Override
                  public void ack() {
                    plugin.getOfflineBanList().remove(banned.getPlayerUUID());
                  }
                });
                break;
            }
          } catch (IOException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (ClassNotFoundException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (TooLargeException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          }
        }
      });
    }

    ConnectionPool.release(client);

    if (plugin.lastSyncs.getProperty("v2", "false").equals("false")) {
      plugin.lastID = -1;
    }
    if (plugin.lastID == -1) {
      plugin.getOfflineBanList().clear();
    }
    plugin.syncRunning = true;
    try {
      client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
      try {
        AtomicLong lastBanId = new AtomicLong(plugin.lastID);
        AtomicLong x = new AtomicLong(0);
        BanSyncClient.cast(client).getBanSync(plugin.lastID, new Client.ResponseHandler() {
          @Override
          public void bans(List<Ban> bans) {
            bans.stream().forEach(
              ban -> {
                if (ban.getId() > lastBanId.get()) lastBanId.set(ban.getId());
                if (ban.getPlayer() != null && ban.getPlayer().getUuid() != null && ban.getPlayer().getUuid().length() == 32) {
                  plugin.getOfflineBanList().addBan(ban.getPlayer().getUuid(), new BannedPlayer(
                    Long.valueOf(ban.getId()),
                    ban.getType(),
                    ((ban.getPlayer() != null) ? ban.getPlayer().getName() : null),
                    ((ban.getPlayer() != null) ? ban.getPlayer().getUuid() : null),
                    ban.getReason(),
                    ((ban.getServer() != null) ? ban.getServer().getAddress() : null),
                    ((ban.getAdmin() != null) ? ban.getAdmin().getName() : null),
                    ((ban.getAdmin() != null) ? ban.getAdmin().getUuid() : null),
                    ban.getDate(),
                    Long.valueOf(ban.getDuration())
                  ));
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
        ConnectionPool.release(client);
        if(plugin.getConfigs().isDebug())
          plugin.debug("Received bans from: " + plugin.lastID + " to: " + lastBanId.get());
        plugin.lastID = lastBanId.get();
        plugin.lastSync = System.currentTimeMillis() / 1000;
        if (responder != null) responder.ack();
        syncSave();
        plugin.getOfflineBanList().save();
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
      if (plugin.getConfigs().isDebug())
        ioException.printStackTrace();
    } catch (BadApiKeyException badApiKeyException) {
      if (plugin.getConfigs().isDebug())
        badApiKeyException.printStackTrace();
    } catch (InterruptedException interruptedException) {
      if (plugin.getConfigs().isDebug())
        interruptedException.printStackTrace();
    } catch (TooLargeException tooLargeException) {
      if (plugin.getConfigs().isDebug())
        tooLargeException.printStackTrace();
    } finally {
      plugin.syncRunning = false;
    }
  }

  public void downloadBannedPlayersJSON(Responder responder) {
    try {
      Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
      try {
        File file = new File(plugin.getDataFolder(), "banned-players.json");
        file.createNewFile();
        OutputStream out = new FileOutputStream(file);
        AtomicLong x = new AtomicLong(0);
        out.write("[".getBytes(StandardCharsets.UTF_8));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss Z");
        OutputStream finalOut = out;
        BanSyncClient.cast(client).getBanSync(0, new Client.ResponseHandler() {
          @Override
          public void bans(List<Ban> bans) {
            bans.stream().filter(
              ban -> !ban.getType().equals("temp")
            ).forEach(
              ban -> {
                try {
                  if (ban.getPlayer() != null && ban.getPlayer().getUuid() != null && ban.getPlayer().getUuid().length() == 32) {
                    finalOut.write((((x.get() == 0) ? "" : ",") + "\n{" +
                      "\n  \"uuid\":\"" + MCBans.fromString(ban.getPlayer().getUuid()) + "\"" +
                      ",\n  \"name\":\"" + ban.getPlayer().getName() + "\"" +
                      ",\n  \"created\":\"" + dateFormat.format(ban.getDate()) + "\"" +
                      ",\n  \"expires\":\"forever\"" +
                      ",\n  \"source\":\"" + ban.getId() + ":mcbans:" + ((ban.getAdmin() != null) ? ban.getAdmin().getName() : "console") + "\"" +
                      ",\n  \"reason\":" + new Gson().toJson(ban.getReason()) +
                      "\n}").getBytes(StandardCharsets.UTF_8));
                  }
                } catch (IOException e) {
                  if (plugin.getConfigs().isDebug())
                    e.printStackTrace();
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
        out.write("]".getBytes(StandardCharsets.UTF_8));
        out.close();
        ConnectionPool.release(client);
        if (responder != null) responder.ack();
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
      if (plugin.getConfigs().isDebug())
        ioException.printStackTrace();
    } catch (BadApiKeyException badApiKeyException) {
      if (plugin.getConfigs().isDebug())
        badApiKeyException.printStackTrace();
    } catch (InterruptedException interruptedException) {
      if (plugin.getConfigs().isDebug())
        interruptedException.printStackTrace();
    } catch (TooLargeException tooLargeException) {
      if (plugin.getConfigs().isDebug())
        tooLargeException.printStackTrace();
    }
  }

  public void start() {
    int callbackInterval = 30 * 1000;
    if(plugin.getConfigs().getSyncInterval()>30){
      callbackInterval= 1000 * plugin.getConfigs().getSyncInterval();
    }
    int finalCallbackInterval = callbackInterval;
    new Thread(() -> {
      new Timer().scheduleAtFixedRate(new TimerTask() {
        public void run() {
          if (plugin.getConfigs().isEnableAutoSync()) {
            try {
              startSync(new Responder(){
                @Override
                public void ack() {
                  if(plugin.getConfigs().isDebug())
                    plugin.getLog().info("Completed ban sync.");
                }

                @Override
                public void error() {
                  if(plugin.getConfigs().isDebug())
                    plugin.getLog().info("Failed to complete sync.");
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
            plugin.syncRunning = false;
          }
        }
      }, 0, finalCallbackInterval); // repeat every 5 minutes.
    }).start();
  }

  public void syncSave() {
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
