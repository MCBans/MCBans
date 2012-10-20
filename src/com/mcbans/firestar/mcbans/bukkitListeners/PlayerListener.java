package com.mcbans.firestar.mcbans.bukkitListeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.pluginInterface.Disconnect;

import org.bukkit.ChatColor;
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
                    if (s3.length >= 4) {
                        if (s3[0].equals("l") || s3[0].equals("g") || s3[0].equals("t") || s3[0].equals("i") || s3[0].equals("s")) {
                            event.disallow(Result.KICK_BANNED, s3[1]);
                            return;
                        } else if (repMin > Double.valueOf(s3[2])) {
                            event.disallow(Result.KICK_BANNED, "Reputation too low!");
                            return;
                        } else if (maxAlts < Integer.valueOf(s3[3])) {
                            event.disallow(Result.KICK_BANNED, "You have too many alternate accounts!");
                            return;
                        }else{
                            HashMap<String, String> tmp = new HashMap<String, String>();
                            if(s3[0].equals("b")){
                                tmp.put("b", "y");
                            }
                            if(Integer.parseInt(s3[3])>0){
                                tmp.put("a", s3[3]);
                                tmp.put("al", s3[6]);
                            }
                            if(s3[4].equals("y")){
                                tmp.put("m", "y");
                            }
                            if(Integer.parseInt(s3[5])>0){
                                tmp.put("d", s3[5]);
                            }
                            MCBans.playerCache.put(event.getName(),tmp);
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
        MCBans.Permissions.playerConnect(event.getPlayer());

        String playerName = event.getPlayer().getName();
        HashMap<String,String> pcache = MCBans.playerCache.remove(playerName);

        if(pcache.containsKey("b")){
            MCBans.broadcastPlayer(playerName, ChatColor.DARK_RED + "You have bans on record! ( check http://mcbans.com )" );
            MCBans.broadcastJoinView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", playerName ) );
        }
        if(pcache.containsKey("d")){
            MCBans.broadcastPlayer(playerName, ChatColor.DARK_RED + pcache.get("d") + " open disputes!" );
        }
        if(pcache.containsKey("a")){
            MCBans.broadcastAltView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", playerName, pcache.get("al").toString() ));
        }
        if(pcache.containsKey("m")){
            MCBans.log( LogLevels.INFO, playerName + " is a MCBans.com Staff member");
            MCBans.broadcastJoinView( ChatColor.AQUA + MCBans.Language.getFormat( "isMCBansMod", playerName ), playerName);
            MCBans.broadcastPlayer(playerName, ChatColor.AQUA + MCBans.Language.getFormat ("youAreMCBansStaff"));
        }
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