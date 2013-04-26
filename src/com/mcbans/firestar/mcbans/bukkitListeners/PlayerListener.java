package com.mcbans.firestar.mcbans.bukkitListeners;

import java.net.InetAddress;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.ConfigurationManager;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.request.DisconnectRequest;
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

    //@EventHandler(priority = EventPriority.HIGHEST) // not called here
    public void onAsyncPlayerPreLoginEvent(final AsyncPlayerPreLoginEvent event) {
        /*
        try {
            int check = 1;
            while (plugin.apiServer == null) {
                // waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                check++;
                if (check > 5) {
                    // can't reach mcbans servers
                    if (config.isFailsafe()){
                        log.warning("Can't reach MCBans API Servers! Kicked player: " + event.getName());
                        event.disallow(Result.KICK_BANNED, _("unavailable"));
                    }else{
                        log.warning("Can't reach MCBans API Servers! Check passed player: " + event.getName());
                    }
                    return;
                }
            }

            // get player information
            final String uriStr = "http://" + plugin.apiServer + "/v2/" + config.getApiKey() + "/login/"
                    + URLEncoder.encode(event.getName(), "UTF-8") + "/"
                    + URLEncoder.encode(String.valueOf(event.getAddress().getHostAddress()), "UTF-8") + "/"
                    + plugin.apiRequestSuffix;
            final URLConnection conn = new URL(uriStr).openConnection();

            conn.setConnectTimeout(config.getTimeoutInSec() * 1000);
            conn.setReadTimeout(config.getTimeoutInSec() * 1000);
            conn.setUseCaches(false);

            BufferedReader br = null;
            String response = null;
            try{
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = br.readLine();
            }finally{
                if (br != null) br.close();
            }
            if (response == null){
                if (config.isFailsafe()){
                    log.warning("Null response! Kicked player: " + event.getName());
                    event.disallow(Result.KICK_BANNED, _("unavailable"));
                }else{
                    log.warning("Null response! Check passed player: " + event.getName());
                }
                return;
            }

            plugin.debug("Response: " + response);
            String[] s = response.split(";");
            if (s.length == 6 || s.length == 7 || s.length == 8) {
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
                        if (s.length == 8){
                            tmp.put("b", s[7]);
                        }else{
                            tmp.put("b", null);
                        }
                    }
                    if(Integer.parseInt(s[3]) > 0){
                        tmp.put("a", s[3]);
                        tmp.put("al", s[6]);
                    }
                    if(s[4].equals("y")){
                        tmp.put("m", "y");
                    }
                    if(Integer.parseInt(s[5]) > 0){
                        tmp.put("d", s[5]);
                    }
                    if (s.length == 8){
                       
                    }
                    plugin.playerCache.put(event.getName(), tmp);
                }
                plugin.debug(event.getName() + " authenticated with " + s[2] + " rep");
            }else{
                if (response.toString().contains("Server Disabled")) {
                    Util.message(Bukkit.getConsoleSender(), ChatColor.RED + "This Server Disabled by MCBans Administration!");
                    return;
                }

                if (config.isFailsafe()){
                    log.warning("Invalid response!(" + s.length + ") Kicked player: " + event.getName());
                    event.disallow(Result.KICK_BANNED, _("unavailable"));
                }else{
                    log.warning("Invalid response!(" + s.length + ") Check passed player: " + event.getName());
                }
                log.warning("Response: " + response);
                return;
            }
        }
        catch (SocketTimeoutException ex){
            log.warning("Cannot connect MCBans API server: timeout");
            if (config.isFailsafe()){
                event.disallow(Result.KICK_BANNED, _("unavailable"));
            }
        }
        catch (IOException ex){
            log.warning("Cannot connect MCBans API server!");
            if (config.isDebug()) ex.printStackTrace();

            if (config.isFailsafe()){
                event.disallow(Result.KICK_BANNED, _("unavailable"));
            }
        }
        catch (Exception ex){
            log.warning("Error occurred in AsyncPlayerPreLoginEvent. Please report this!");
            ex.printStackTrace();

            if (config.isFailsafe()){
                log.warning("Internal exception! Kicked player: " + event.getName());
                event.disallow(Result.KICK_BANNED, _("unavailable"));
            }
        }
        */
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // check player connected from proxy
        if (!checkConnectedFrom(player)){
            final String ip = player.getAddress().getAddress().getHostAddress();
            log.severe("[MCBans] ERROR! Invalid connection address(" + ip + ")! Disabling plugin..");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        
        if (config.isSendJoinMessage()){
            Util.message(player, ChatColor.DARK_GREEN + "Server secured by MCBans!");
        }
    }
    private boolean checkConnectedFrom(final Player player){
        // check if the server is using spigot (which relays the real player ip now in combination with BungeeCord)
        try {
            Class.forName("org.spigotmc.Metrics", false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {}
        
        if (player == null || player.getAddress() == null){
            return false;
        }
        
        final InetAddress inet = player.getAddress().getAddress();
        if (inet == null || inet.getHostAddress() == null){
            return false;
        }
        
        // check player connected from local network
        if (inet.isAnyLocalAddress() || inet.isSiteLocalAddress() || inet.isLoopbackAddress()){
            return true;
        }
        
        // check from proxy address
        return inet.getHostAddress().equalsIgnoreCase(config.getProxyIP());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // send disconnect request
        new Thread(new DisconnectRequest(plugin, event.getPlayer().getName())).start();
        
        if (plugin.mcbStaff.contains(event.getPlayer().getName())){
            plugin.mcbStaff.remove(event.getPlayer().getName());
        }
    }
}