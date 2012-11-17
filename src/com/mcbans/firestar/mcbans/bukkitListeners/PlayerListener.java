package com.mcbans.firestar.mcbans.bukkitListeners;

import static com.mcbans.firestar.mcbans.I18n._;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.ConfigurationManager;
import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.pluginInterface.Disconnect;
import com.mcbans.firestar.mcbans.util.Util;

public class PlayerListener implements Listener {
    private final MCBans plugin;
    private final ActionLog log;
    private final ConfigurationManager config;

    public PlayerListener(final MCBans plugin) {
        this.plugin = plugin;
        this.log = plugin.getLog();
        this.config = plugin.getConfigs();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(final AsyncPlayerPreLoginEvent event) {
        try {
            int check = 1;
            while (plugin.apiServer == null) {
                // waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                check++;
                if (check > 5) {
                    log.warning("Can't reach MCBans API Servers! Check passed player: " + event.getName());
                    return;
                }
            }

            // get player information
            final URL urlMCBans = new URL("http://" + plugin.apiServer + "/v2/" + config.getApiKey() + "/login/"
                    + URLEncoder.encode(event.getName(), "UTF-8") + "/"
                    + URLEncoder.encode(String.valueOf(event.getAddress().getHostAddress()), "UTF-8"));
            BufferedReader br = null;
            String response = null;
            try{
                br = new BufferedReader(new InputStreamReader(urlMCBans.openStream()));
                response = br.readLine();
            }finally{
                if (br != null) br.close();
            }
            if (response == null){
                log.warning("Null response! (Player: " + event.getName() + ")");
                return;
            }

            plugin.debug("Response: " + response);
            String[] s = response.split(";");
            if (s.length == 6 || s.length == 7) {
                // check banned
                if (s[0].equals("l") || s[0].equals("g") || s[0].equals("t") || s[0].equals("i") || s[0].equals("s")) {
                    event.disallow(Result.KICK_BANNED, s[1]);
                    return;
                }
                // check reputation
                else if (config.getMinRep() > Double.valueOf(s[2])) {
                    event.disallow(Result.KICK_BANNED, _("underMinRep"));
                    return;
                }
                // check alternate accounts
                else if (config.isEnableMaxAlts() && config.getMaxAlts() < Integer.valueOf(s[3])) {
                    event.disallow(Result.KICK_BANNED, _("overMaxAlts"));
                    return;
                }
                // check passed, put data to playerCache
                else{
                    HashMap<String, String> tmp = new HashMap<String, String>();
                    if(s[0].equals("b")){
                        tmp.put("b", "y");
                    }
                    if(Integer.parseInt(s[3])>0){
                        tmp.put("a", s[3]);
                        tmp.put("al", s[6]);
                    }
                    if(s[4].equals("y")){
                        tmp.put("m", "y");
                    }
                    if(Integer.parseInt(s[5])>0){
                        tmp.put("d", s[5]);
                    }
                    plugin.playerCache.put(event.getName(), tmp);
                }
                plugin.debug(event.getName() + " authenticated with " + s[2] + " rep");
            }else{
                log.warning("Invalid response! Player: " + event.getName() + ", length: " + s.length);
                log.warning("Response: " + response);
            }
        }catch (Exception ex){
            log.warning("Error occurred in AsyncPlayerPreLoginEvent. Please report this!");
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        final HashMap<String,String> pcache = plugin.playerCache.remove(player.getName());
        if(pcache == null) return;

        if(pcache.containsKey("b")){
            if (config.isEnableSendPreviousBans())
                Util.message(player, ChatColor.DARK_RED + _("bansOnRecord"));
            Perms.VIEW_BANS.message(ChatColor.DARK_RED + _("previousBans", I18n.PLAYER, player.getName()));
        }
        if(pcache.containsKey("d")){
            Util.message(player, ChatColor.DARK_RED + _("disputes", I18n.COUNT, pcache.get("d")));
        }
        if(pcache.containsKey("a")){
            Perms.VIEW_ALTS.message(ChatColor.DARK_PURPLE + _("altAccounts", I18n.PLAYER, player.getName(), I18n.ALTS, pcache.get("al")));
        }
        if(pcache.containsKey("m")){
            log.info(player.getName() + " is a MCBans Staff member");
            Util.broadcastMessage(ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
            Util.message(player, ChatColor.AQUA + "You are a MCBans Staff Member! (ver " + plugin.getDescription().getVersion() + ")");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Disconnect disconnectHandler = new Disconnect(plugin, event.getPlayer().getName());
        (new Thread(disconnectHandler)).start();
    }
}