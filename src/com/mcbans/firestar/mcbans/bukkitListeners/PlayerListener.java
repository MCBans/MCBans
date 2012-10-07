package com.mcbans.firestar.mcbans.bukkitListeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.Connect;
import com.mcbans.firestar.mcbans.pluginInterface.Disconnect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private BukkitInterface MCBans;

    public PlayerListener(BukkitInterface plugin) {
        MCBans = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        try {
            int check = 1;
            while (MCBans.notSelectedServer) {
                // waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                check++;
                if (check > 5) {
                    break;
                }
            }
            if (check <= 5) {
                URL urlMCBans = new URL("http://" + MCBans.apiServer + "/v2/" + MCBans.getApiKey() + "/login/"
                        + URLEncoder.encode(event.getName(), "UTF-8") + "/"
                        + URLEncoder.encode(String.valueOf(event.getAddress().getHostAddress()), "UTF-8"));
                BufferedReader bufferedreaderMCBans = new BufferedReader(new InputStreamReader(urlMCBans.openStream()));
                String s2 = bufferedreaderMCBans.readLine();
                System.out.println(s2);
                bufferedreaderMCBans.close();
                if (s2 != null) {
                    String[] s3 = s2.split(";");
                    double repMin = MCBans.Settings.getDouble("minRep");
                    int maxAlts = MCBans.Settings.getInteger("maxAlts");
                    if (s3.length == 4) {
                        if (s3[0].equals("l") || s3[0].equals("g") || s3[0].equals("t") || s3[0].equals("i") || s3[0].equals("s")) {
                            event.disallow(Result.KICK_BANNED, s3[1]);
                            return;
                        } else if (repMin > Double.valueOf(s3[2])) {
                            event.disallow(Result.KICK_BANNED, "Reputation too low!");
                            return;
                        } else if (maxAlts < Integer.valueOf(s3[3])) {
                            event.disallow(Result.KICK_BANNED, "You have too many alternate accounts!");
                            return;
                        }
                        if (MCBans.Settings.getBoolean("isDebug")) {
                            System.out.println("[MCBans] " + event.getName() + " authenticated with " + s3[2] + " rep");
                        }
                    }
                }
            }
        } catch (IOException e) {
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
            // e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerIP = event.getPlayer().getAddress().getAddress().getHostAddress();
        Player player = event.getPlayer();
        MCBans.Permissions.playerConnect(player);
        Connect playerConnect = new Connect();
        playerConnect.ConnectSet(MCBans, player.getName(), playerIP);
        (new Thread(playerConnect)).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MCBans.Permissions.playerDisconnect(player.getName());
        String playerName = player.getName();
        Disconnect disconnectHandler = new Disconnect(MCBans, playerName);
        (new Thread(disconnectHandler)).start();
    }
}