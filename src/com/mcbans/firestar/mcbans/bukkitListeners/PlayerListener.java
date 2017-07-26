package com.mcbans.firestar.mcbans.bukkitListeners;

import static com.mcbans.firestar.mcbans.I18n._;

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
import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.ConfigurationManager;
import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.DisconnectRequest;
import com.mcbans.firestar.mcbans.util.Util;

public class PlayerListener implements Listener {
    private final MCBans plugin;
    private final ActionLog log;
    private final ConfigurationManager config;
    public static Cache<String, String> cache = CacheBuilder.newBuilder()
    	    .maximumSize(10000)
    	    .expireAfterWrite(5, TimeUnit.MINUTES)
    	    .build();
    public PlayerListener(final MCBans plugin) {
        this.plugin = plugin;
        this.log = plugin.getLog();
        this.config = plugin.getConfigs();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLoginEvent(final AsyncPlayerPreLoginEvent event) {
        try {
        	String response = cache.getIfPresent(event.getName().toLowerCase());
        	if(response==null){
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
	                        log.warning("Can't reach the MCBans API Server! Kicked player: " + event.getName());
	                        event.disallow(Result.KICK_BANNED, _("unavailable"));
	                    }else{
	                        log.warning("Can't reach the MCBans API Server! Check passed player: " + event.getName());
	                    }
	                    return;
	                }
	            }
	            
	            // get player information
	            final String uriStr = "http://" + plugin.apiServer + "/v3/" + config.getApiKey() + "/login/"
	                    + URLEncoder.encode(event.getName(), "UTF-8") + "/"
	                    + URLEncoder.encode(event.getAddress().getHostAddress(), "UTF-8") + "/"
	                    + plugin.apiRequestSuffix;
	            final URLConnection conn = new URL(uriStr).openConnection();
	
	            conn.setConnectTimeout(config.getTimeoutInSec() * 1000);
	            conn.setReadTimeout(config.getTimeoutInSec() * 1000);
	            conn.setUseCaches(false);
	
	            BufferedReader br = null;
	            response = null;
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
	            cache.put(event.getName().toLowerCase(), response);
        	}else{
        		plugin.debug("Retrieved from cache");
        	}
            plugin.debug("Response: " + response);
            handleConnectionData(response,event);
        }
        catch (SocketTimeoutException ex){
            log.warning("Cannot connect to the MCBans API server: timeout");
            if (config.isFailsafe()){
                event.disallow(Result.KICK_BANNED, _("unavailable"));
            }
        }
        catch (IOException ex){
            log.warning("Cannot connect to the MCBans API server!");
            if (config.isDebug()) ex.printStackTrace();

            if (config.isFailsafe()){
                event.disallow(Result.KICK_BANNED, _("unavailable"));
            }
        }
        catch (Exception ex){
            log.warning("An error occurred in AsyncPlayerPreLoginEvent. Please report this!");
            ex.printStackTrace();

            if (config.isFailsafe()){
                log.warning("Internal exception! Kicked player: " + event.getName());
                event.disallow(Result.KICK_BANNED, _("unavailable"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
    	if(!event.getPlayer().getServer().getOnlineMode() ){
    		(new Thread(new HandleConnectionData(event))).start(); // push to another thread to process the connection for bungeecord, does not save history
    	}else{
	        final Player player = event.getPlayer();
	        final HashMap<String,String> pcache = plugin.playerCache.remove(player.getName());
	        if(pcache == null) return;
	        if(pcache.containsKey("b")){
	            Util.message(player, ChatColor.RED + _("bansOnRecord"));
	            Perms.VIEW_BANS.message(ChatColor.RED + _("previousBans", I18n.PLAYER, player.getName()));
	            if (!Perms.HIDE_VIEW.has(player)){
	                String prev = pcache.get("b");
	                if (config.isSendDetailPrevBans() && prev != null){
	                    prev = prev.trim();
	                    String[] bans = prev.split(",");
	                    for (String ban : bans){
	                        String[] data = ban.split("\\$");
	                        if (data.length == 3){
	                            Perms.VIEW_BANS.message(ChatColor.WHITE+ data[1] + ChatColor.GRAY + " .:. " + ChatColor.WHITE + data[0] + ChatColor.GRAY +  " (by " + data[2] + ")");
	                        }
	                    }
	                }
	            }
	        }
	        if(pcache.containsKey("d")){
	            Util.message(player, ChatColor.RED + _("disputes", I18n.COUNT, pcache.get("d")));
	        }
	        if(pcache.containsKey("pn")){
	        	StringBuilder plist = new StringBuilder();
	        	for(String name: pcache.get("pn").split(",")){
	        		plist.append(plist.length() == 0 ? "" : ", ").append(name);
	        	}
	        	 Perms.VIEW_PREVIOUS.message(ChatColor.RED + _("previousNames", I18n.PLAYER, player.getName(), I18n.PLAYERS, plist.toString() ));
	        }
	        if(pcache.containsKey("dnsbl")){
	        	StringBuilder proxlist = new StringBuilder();
	        	for(String name: pcache.get("dnsbl").split(",")){
	        		String from = name.split("$")[0];
	        		String reason = name.split("$")[1];
	        		proxlist.append(proxlist.length() == 0 ? "" : ", ").append("[ ").append(from).append(" { ").append(reason).append(" } ]");
	        	}
	        	 Perms.VIEW_PROXY.message(ChatColor.RED + _("proxyDetected", I18n.PLAYER, player.getName(), I18n.REASON, proxlist.toString() ));
	        }
	        if(pcache.containsKey("a")){
	            if (!Perms.HIDE_VIEW.has(player))
	                Perms.VIEW_ALTS.message(ChatColor.DARK_PURPLE + _("altAccounts", I18n.PLAYER, player.getName(), I18n.ALTS, pcache.get("al")));
	        }
	        if(pcache.containsKey("m")){
	            //Util.broadcastMessage(ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
	            // notify to console, mcbans.view.staff, mcbans.admin, mcbans.ban.global players
	            Util.message(Bukkit.getConsoleSender(), ChatColor.AQUA + player.getName() + " is an MCBans Staff member");
	            
	            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
	                @Override
	                public void run() {
	                    Set<Player> players = Perms.VIEW_STAFF.getPlayers();
	                    players.addAll(Perms.ADMIN.getPlayers());
	                    players.addAll(Perms.BAN_GLOBAL.getPlayers());
	                    for (final Player p : players){
	                        if (p.canSee(player)){ // check joined player cansee
	                            Util.message(p, ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
	                        }
	                    }
	                }
	            }, 1L);
	            
	            // send information to mcbans staff
	            Set<String> admins = new HashSet<String>();
	            for (Player p : Perms.ADMIN.getPlayers()){
	                admins.add(p.getName());
	            }
	            Util.message(player, ChatColor.AQUA + "You are an MCBans Staff Member! (ver " + plugin.getDescription().getVersion() + ")");
	            Util.message(player, ChatColor.AQUA + "Online Admins: " + ((admins.size() > 0) ? Util.join(admins, ", ") : ChatColor.GRAY + "(none)"));
	           
	            // add online mcbans staff list array
	            plugin.mcbStaff.add(player.getName());
	        }
	        if (config.isSendJoinMessage()){
	            Util.message(player, ChatColor.DARK_GREEN + "Server secured by MCBans!");
	        }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        // send disconnect request
        (new Thread(new DisconnectRequest(plugin, event.getPlayer().getName()))).start();
        
        if (plugin.mcbStaff.contains(event.getPlayer().getName())){
            plugin.mcbStaff.remove(event.getPlayer().getName());
        }
    }
    private class HandleConnectionData implements Runnable{
    	private PlayerJoinEvent event = null;
    	public HandleConnectionData(PlayerJoinEvent event){
    		this.event = event;
    	}
		@SuppressWarnings("unused")
		@Override
	    public void run(){
			String response = "";
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
                            log.warning("Can't reach the MCBans API Servers! Kicked player: " + event.getPlayer().getName());
                        }else{
                            log.warning("Can't reach the MCBans API Servers! Check passed player: " + event.getPlayer().getName());
                        }
                        return;
                    }
                }

                // get player information
                final String uriStr = "http://" + plugin.apiServer + "/v3/" + config.getApiKey() + "/details/"
                        + URLEncoder.encode(event.getPlayer().getName(), "UTF-8") + "/"
                        + URLEncoder.encode(event.getPlayer().getAddress().getAddress().getHostAddress(), "UTF-8") + "/"
                        + plugin.apiRequestSuffix;
                final URLConnection conn = new URL(uriStr).openConnection();

                conn.setConnectTimeout(config.getTimeoutInSec() * 1000);
                conn.setReadTimeout(config.getTimeoutInSec() * 1000);
                conn.setUseCaches(false);

                BufferedReader br = null;
               
                try{
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response = br.readLine();
                }finally{
                    if (br != null) br.close();
                }

                plugin.debug("Response: " + response);
	    	}
            catch (SocketTimeoutException ex){
                log.warning("Cannot connect to the MCBans API server: timeout");
            }
            catch (IOException ex){
                log.warning("Cannot connect to the MCBans API server!");
                if (config.isDebug()) ex.printStackTrace();
            }
            catch (Exception ex){
                log.warning("An error occurred in AsyncPlayerPreLoginEvent. Please report this!");
                ex.printStackTrace();
            }
	    	String[] s = response.split(";");
	    	if (s.length >= 6) {
	            // put data to playerCache
	            HashMap<String, String> pcache = new HashMap<String, String>();
	            if(s[0].equals("b")){
	            	if(s.length>=8){
	            		pcache.put("b", s[7]);
	            	}
	            }
	            if(Integer.parseInt(s[3]) > 0){
	            	if(s.length>=7){
	            		pcache.put("a", s[3]);
	            		pcache.put("al", s[6]);
	            	}
	            }
	            if(s.length>=5){
	                if(s[4].equals("y")){
	                	pcache.put("m", "y");
	                }
	            }
	            if(s.length>=6){
	                if(Integer.parseInt(s[5]) > 0){
	                	pcache.put("d", s[5]);
	                }
	            }
	            if(s.length>=9){
	            	if(!s[8].equals("")){
	            		pcache.put("pn", s[8]);
	            	}
	            }
	            if(s.length>=10){
	            	if(!s[9].equals("")){
	            		pcache.put("dnsbl", s[9]);
	            	}
	        	}
	            final Player player = event.getPlayer();
		        if(pcache == null) return;
		        if(pcache.containsKey("b")){
		            Util.message(player, ChatColor.RED + _("bansOnRecord"));
		            Perms.VIEW_BANS.message(ChatColor.RED + _("previousBans", I18n.PLAYER, player.getName()));
		            if (!Perms.HIDE_VIEW.has(player)){
		                String prev = pcache.get("b");
		                if (config.isSendDetailPrevBans() && prev != null){
		                    prev = prev.trim();
		                    String[] bans = prev.split(",");
		                    for (String ban : bans){
		                        String[] data = ban.split("\\$");
		                        if (data.length == 3){
		                            Perms.VIEW_BANS.message(ChatColor.WHITE+ data[1] + ChatColor.GRAY + " .:. " + ChatColor.WHITE + data[0] + ChatColor.GRAY +  " (by " + data[2] + ")");
		                        }
		                    }
		                }
		            }
		        }
		        if(pcache.containsKey("d")){
		            Util.message(player, ChatColor.RED + _("disputes", I18n.COUNT, pcache.get("d")));
		        }
		        if(pcache.containsKey("pn")){
		        	StringBuilder plist = new StringBuilder();
		        	for(String name: pcache.get("pn").split(",")){
		        		plist.append(plist.length() == 0 ? "" : ", ").append(name);
		        	}
		        	 Perms.VIEW_PREVIOUS.message(ChatColor.RED + _("previousNames", I18n.PLAYER, player.getName(), I18n.PLAYERS, plist.toString() ));
		        }
		        if(pcache.containsKey("dnsbl")){
		        	StringBuilder proxlist = new StringBuilder();
		        	for(String name: pcache.get("dnsbl").split(",")){
		        		String from = name.split("$")[0];
		        		String reason = name.split("$")[1];
		        		proxlist.append(proxlist.length() == 0 ? "" : ", ").append("[ ").append(from).append(" { ").append(reason).append(" } ]");
		        	}
		        	 Perms.VIEW_PROXY.message(ChatColor.RED + _("proxyDetected", I18n.PLAYER, player.getName(), I18n.REASON, proxlist.toString() ));
		        }
		        if(pcache.containsKey("a")){
		            if (!Perms.HIDE_VIEW.has(player))
		                Perms.VIEW_ALTS.message(ChatColor.DARK_PURPLE + _("altAccounts", I18n.PLAYER, player.getName(), I18n.ALTS, pcache.get("al")));
		        }
		        if(pcache.containsKey("m")){
		            //Util.broadcastMessage(ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
		            // notify to console, mcbans.view.staff, mcbans.admin, mcbans.ban.global players
		            Util.message(Bukkit.getConsoleSender(), ChatColor.AQUA + player.getName() + " is an MCBans Staff member");
		            
		            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
		                @Override
		                public void run() {
		                    Set<Player> players = Perms.VIEW_STAFF.getPlayers();
		                    players.addAll(Perms.ADMIN.getPlayers());
		                    players.addAll(Perms.BAN_GLOBAL.getPlayers());
		                    for (final Player p : players){
		                        if (p.canSee(player)){ // check joined player cansee
		                            Util.message(p, ChatColor.AQUA + _("isMCBansMod", I18n.PLAYER, player.getName()));
		                        }
		                    }
		                }
		            }, 1L);
		            
		            // send information to mcbans staff
		            Set<String> admins = new HashSet<String>();
		            for (Player p : Perms.ADMIN.getPlayers()){
		                admins.add(p.getName());
		            }
		            Util.message(player, ChatColor.AQUA + "You are an MCBans Staff Member! (ver " + plugin.getDescription().getVersion() + ")");
		            Util.message(player, ChatColor.AQUA + "Online Admins: " + ((admins.size() > 0) ? Util.join(admins, ", ") : ChatColor.GRAY + "(none)"));
		           
		            // add online mcbans staff list array
		            plugin.mcbStaff.add(player.getName());
		        }
		        if (config.isSendJoinMessage()){
		            Util.message(player, ChatColor.DARK_GREEN + "Server secured by MCBans!");
		        }
	        }else{
	            if (response.toString().contains("Server Disabled")) {
	                Util.message(Bukkit.getConsoleSender(), ChatColor.RED + "This Server Has Been Disabled by MCBans Staff! Please go to forums.mcbans.com!");
	                return;
	            }
	            log.warning("Response: " + response);
	            return;
	        }
	    }
    }
    public void handleConnectionData(String response, AsyncPlayerPreLoginEvent event){
    	String[] s = response.split(";");
    	if (s.length >= 5) {
            // check banned
            if (s[0].equals("l") || s[0].equals("g") || s[0].equals("t") || s[0].equals("i") || s[0].equals("s")) {
            	String[] reasonData = s[1].split("\\$");
                event.disallow(Result.KICK_BANNED, _("banReturnMessage", I18n.REASON, reasonData[0], I18n.ADMIN, reasonData[1], I18n.BANID, reasonData[2], I18n.TYPE, reasonData[3]));
                return;
            }
            // check reputation
            else if (config.getMinRep() > Double.valueOf(s[2])) {
                event.disallow(Result.KICK_BANNED, _("underMinRep"));
                return;
            }
            // check alternate accounts
            else if (config.isEnableMaxAlts() && config.getMaxAlts() < Integer.valueOf(s[3]) && !Perms.EXEMPT_MAXALTS.has(event.getName())) {
                event.disallow(Result.KICK_BANNED, _("overMaxAlts"));
                return;
            }
            // check passed, put data to playerCache
            else{
                HashMap<String, String> tmp = new HashMap<String, String>();
                if(s[0].equals("b")){
                	if(s.length>=8){
                		tmp.put("b", s[7]);
                	}
                }
                if(Integer.parseInt(s[3]) > 0){
                	if(s.length>=7){
                        tmp.put("a", s[3]);
                        tmp.put("al", s[6]);
                	}
                }
                if(s.length>=5){
                    if(s[4].equals("y")){
                        tmp.put("m", "y");
                    }
                }
                if(s.length>=6){
                    if(Integer.parseInt(s[5]) > 0){
                        tmp.put("d", s[5]);
                    }
                }
                if(s.length>=9){
                	if(!s[8].equals("")){
                		tmp.put("pn", s[8]);
                	}
                }
                if(s.length>=10){
                	if(!s[9].equals("")){
                		tmp.put("dnsbl", s[9]);
                	}
            	}
                if (s.length == 8){
                   
                }
                plugin.playerCache.put(event.getName(), tmp);
            }
            plugin.debug(event.getName() + " authenticated with " + s[2] + " rep");
        }else{
            if (response.toString().contains("Server Disabled")) {
                Util.message(Bukkit.getConsoleSender(), ChatColor.RED + "This Server Has Been Disabled by MCBans Staff! Please go to forums.mcbans.com!");
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
}