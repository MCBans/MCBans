package com.mcbans.plugin.request;

import static com.mcbans.plugin.I18n.localize;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.mcbans.client.response.BanResponse;
import com.mcbans.plugin.bukkitListeners.PlayerListener;
import com.mcbans.plugin.events.PlayerGlobalBanEvent;
import com.mcbans.plugin.events.PlayerLocalBanEvent;
import com.mcbans.plugin.events.PlayerTempBanEvent;
import com.mcbans.plugin.permission.Perms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.BanList;
import org.bukkit.entity.Player;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.I18n;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.events.PlayerBanEvent;
import com.mcbans.plugin.events.PlayerUnbanEvent;
import com.mcbans.plugin.events.PlayerUnbannedEvent;
import com.mcbans.plugin.org.json.JSONException;
import com.mcbans.plugin.org.json.JSONObject;
import com.mcbans.plugin.util.Util;

public class Ban {
    private final MCBans plugin;
    private final ActionLog log;

    private String playerName, playerIP, senderName, reason, action, duration, measure, badword, playerUUID, senderUUID = null;
    private boolean rollback = false;
    private JSONObject actionData = null;
    private HashMap<String, Integer> responses = new HashMap<String, Integer>();
    private int action_id;

    public Ban(MCBans plugin, String action, String playerName, String playerUUID, String playerIP, String senderName, String senderUUID, 
    		String reason, String duration, String measure, JSONObject actionData, boolean rollback) {
    	this(plugin, action, playerName, playerIP, senderName, reason, duration,
                measure, actionData, rollback);
    	this.playerUUID = playerUUID;
    	this.senderUUID = senderUUID;
    }
    public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
            String measure, JSONObject actionData, boolean rollback) {
        this.plugin = plugin;
        this.log = plugin.getLog();

        this.playerName = playerName;
        this.playerIP = playerIP;
        this.senderName = senderName;
        this.reason = reason;
        this.rollback = rollback;
        this.duration = duration;
        this.measure = measure;
        this.action = action;
        this.actionData = (actionData != null) ? actionData : new JSONObject();
        BanResponse res = PlayerListener.cache.getIfPresent(playerName.toLowerCase());
        if(res!=null){
        	PlayerListener.cache.invalidate(playerName.toLowerCase());
        }
        responses.put("globalBan", 0);
        responses.put("localBan", 1);
        responses.put("tempBan", 2);
        responses.put("unBan", 3);
    }

    public Ban(MCBans plugin, String action, String playerName, String playerIP, String senderName, String reason, String duration,
            String measure) {
        this (plugin, action, playerName, playerIP, senderName, reason, duration, measure, null, false);
    }

    public void kickPlayer(String playerName, String playerUUID, final String kickReason) {
    	Player targettmp = null;
    	if(!playerUUID.equals("")){
    		targettmp = MCBans.getPlayer(plugin,playerUUID);;
    	}else{
    		targettmp = plugin.getServer().getPlayerExact(playerName);
    	}
        final Player target = targettmp;
        if (target != null) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()->target.kickPlayer(kickReason), 1);
        }
    }

    public void run() {
        try {

            while (plugin.apiServer == null) {
                // waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            if (responses.containsKey(action)) {
                action_id = responses.get(action);

                // Call BanEvent
                if (action_id != 3) {
                    PlayerBanEvent banEvent = new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure);;
                    plugin.getServer().getPluginManager().callEvent(banEvent);
                    if (banEvent.isCancelled()) {
                        return;
                    }
                    senderName = banEvent.getSenderName();
                    reason = banEvent.getReason();
                    action_id = banEvent.getActionID();
                    duration = banEvent.getDuration();
                    measure = banEvent.getMeasure();
                }
                Player targettmp = null;
                if (!playerUUID.equals("")) {
                    targettmp = MCBans.getPlayer(plugin, playerUUID);
                } else {
                    targettmp = plugin.getServer().getPlayerExact(playerName);
                }
                if (targettmp != null && action_id != 3) {
                    if (Perms.EXEMPT_BAN.has(targettmp)) {
                        Util.message(senderName, ChatColor.RED + localize("banExemptPlayer", I18n.PLAYER, targettmp.getName()));
                        return;
                    }
                } else if (playerName != null && action_id != 3) {
                    if (Perms.EXEMPT_BAN.has(playerName)) {
                        Util.message(senderName, ChatColor.RED + localize("banExemptPlayer", I18n.PLAYER, playerName));
                        return;
                    }
                }
                switch (action_id) {
                    case 0:
                        globalBan();
                        break;
                    case 1:
                        localBan();
                        break;
                    case 2:
                        tempBan();
                        break;
                    case 3:
                        unBan();
                        break;
                }
            } else {
                err();
            }
        } catch (Exception e) {
            e.printStackTrace();
            err();
        }
    }

    public void unBan() {
        // Call PlayerUnbanEvent
        PlayerUnbanEvent unBanEvent = new PlayerUnbanEvent(playerName, playerUUID, senderName, senderUUID);
        plugin.getServer().getPluginManager().callEvent(unBanEvent);
        if (unBanEvent.isCancelled()){
            return;
        }
        senderName = unBanEvent.getSenderName();
        
        // First, remove from bukkit banlist
        if (!Util.isValidIP(playerName)){
            bukkitBan(false);
        }else{
            Bukkit.getScheduler().runTaskLater(plugin, ()->Bukkit.getServer().unbanIP(playerName), 1);
        }
        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> url_items = new HashMap<>();
        url_items.put("player", playerName);
        url_items.put("player_uuid", playerUUID);
        url_items.put("admin", senderName);
        url_items.put("admin_uuid", senderUUID);
        url_items.put("exec", "unBan");
        new Thread(()->{
            HashMap<String, String> response = webHandle.mainRequest(url_items);

            if (response.containsKey("error")){
                Util.message(senderName, ChatColor.RED + "Error: " + response.get("error"));
                return;
            }
            if (!response.containsKey("result")) {
                Util.message(senderName, ChatColor.RED + localize("unBanError", I18n.PLAYER, playerName, I18n.SENDER, senderName));
                return;
            }
            if (response.get("result").equals("y")) {
                if (response.containsKey("player")){
                    playerName = response.get("player");
                }
                if (!Util.isValidIP(playerName)){

                }
                Util.broadcastMessage(ChatColor.GREEN + localize("unBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName));

                Bukkit.getScheduler().runTaskLater(plugin, ()->plugin.getServer().getPluginManager().callEvent(new PlayerUnbannedEvent(playerName, playerUUID, senderName, senderUUID)), 1);

                log.info(senderName + " unbanned " + playerName + "!");
                return;
            } else if (response.get("result").equals("e")) {
                Util.message(senderName, ChatColor.RED + localize("unBanError", I18n.PLAYER, playerName, I18n.SENDER, senderName));
            } else if (response.get("result").equals("s")) {
                Util.message(senderName, ChatColor.RED + localize("unBanGroup", I18n.PLAYER, playerName, I18n.SENDER, senderName));
            } else if (response.get("result").equals("n")) {
                Util.message(senderName, ChatColor.RED + localize("unBanNot", I18n.PLAYER, playerName, I18n.SENDER, senderName));
            }
            log.info(senderName + " tried to unban " + playerName + "!");
        }).start();
    }

    public void localBan() {
        // Call PlayerLocalBanEvent
        PlayerLocalBanEvent lBanEvent = new PlayerLocalBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason);
        plugin.getServer().getPluginManager().callEvent(lBanEvent);
        if (lBanEvent.isCancelled()){
            return;
        }
        senderName = lBanEvent.getSenderName();
        reason = lBanEvent.getReason();
        
        // First, add bukkit banlist
        bukkitBan(true);

        new Thread(()-> {
            JsonHandler webHandle = new JsonHandler(plugin);
            HashMap<String, String> url_items = new HashMap<String, String>();
            url_items.put("player", playerName);
            url_items.put("player_uuid", playerUUID);
            url_items.put("playerip", playerIP);
            url_items.put("reason", reason);
            url_items.put("admin", senderName);
            url_items.put("admin_uuid", senderUUID);
            if (rollback) {
                plugin.getRbHandler().rollback(senderName, playerName);
            }
            if (actionData != null) {
                url_items.put("actionData", actionData.toString());
            }
            url_items.put("exec", "localBan");
            HashMap<String, String> response = webHandle.mainRequest(url_items);
            try {
                if (response.containsKey("error")) {
                    Util.message(senderName, ChatColor.RED + "Error: " + response.get("error"));
                    return;
                }
                if (response.containsKey("player")) {
                    playerName = response.get("player");
                }
                if (!response.containsKey("result")) {
                    Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
                    return;
                }
                if (response.get("result").equals("y")) {

                    this.kickPlayer(playerName, playerUUID, localize("localBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));

                    Util.broadcastMessage(ChatColor.GREEN + localize("localBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));

                    Bukkit.getScheduler().runTaskLater(plugin, ()->plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure)), 1);

                    log.info(playerName + " has been banned with a local type ban [" + reason + "] [" + senderName + "]!");
                    return;
                } else if (response.get("result").equals("e")) {
                    Util.message(senderName,
                        ChatColor.RED + localize("localBanError", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("s")) {
                    Util.message(senderName,
                        ChatColor.RED + localize("localBanGroup", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("a")) {
                    Util.message(senderName,
                        ChatColor.RED + localize("localBanAlready", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                }
                log.info(senderName + " tried to ban " + playerName + " with a local type ban [" + reason + "]!");
            } catch (Exception ex) {
                Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
                log.warning("Error occurred with local banning. Please report this to an MCBans developer.");
                ex.printStackTrace();
            }
        }).start();
    }

    public void globalBan() {
        // Call PlayerGlobalBanEvent
        PlayerGlobalBanEvent gBanEvent = new PlayerGlobalBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason);
        plugin.getServer().getPluginManager().callEvent(gBanEvent);
        if (gBanEvent.isCancelled()){
            return;
        }
        senderName = gBanEvent.getSenderName();
        reason = gBanEvent.getReason();
        
        // First, add bukkit banlist
        bukkitBan(true);
        new Thread(()-> {
            JsonHandler webHandle = new JsonHandler(plugin);
            HashMap<String, String> url_items = new HashMap<String, String>();
            url_items.put("player", playerName);
            url_items.put("player_uuid", playerUUID);
            url_items.put("playerip", playerIP);
            url_items.put("reason", reason);
            url_items.put("admin", senderName);
            url_items.put("admin_uuid", senderUUID);


            if (rollback) {
                plugin.getRbHandler().rollback(senderName, playerName);
            }
            if (actionData.length() > 0) {
                url_items.put("actionData", actionData.toString());
            }
            url_items.put("exec", "globalBan");
            HashMap<String, String> response = webHandle.mainRequest(url_items);
            try {
                if (response.containsKey("error")) {
                    Util.message(senderName, ChatColor.RED + "Error: " + response.get("error"));
                    return;
                }
                if (response.containsKey("player")) {
                    playerName = response.get("player");
                }
                if (!response.containsKey("result")) {
                    Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
                    return;
                }
                if (response.get("result").equals("y")) {
                    this.kickPlayer(playerName, playerUUID, localize("globalBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                    Util.broadcastMessage(ChatColor.GREEN + localize("globalBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));

                    Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure)), 1);

                    log.info(playerName + " has been banned with a global type ban [" + reason + "] [" + senderName + "]!");
                    return;
                } else if (response.get("result").equals("e")) {
                    Util.message(senderName,
                        ChatColor.RED + localize("globalBanError", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("w")) {
                    badword = response.get("word");
                    Util.message(senderName,
                        ChatColor.RED + localize("globalBanWarning", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP, I18n.BADWORD, badword));
                } else if (response.get("result").equals("s")) {
                    Util.message(senderName,
                        ChatColor.RED + localize("globalBanGroup", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("a")) {
                    Util.message(senderName,
                        ChatColor.RED + localize("globalBanAlready", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                }
                log.info(senderName + " tried to ban " + playerName + " with a global type ban [" + reason + "]!");
            } catch (Exception ex) {
                Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");

                log.warning("Error occurred with global banning. Please report this to an MCBans developer.");
                ex.printStackTrace();
            }
        }).start();
    }

    public void tempBan() {
        // Call PlayerTempBanEvent
        PlayerTempBanEvent tBanEvent = new PlayerTempBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, duration, measure);
        plugin.getServer().getPluginManager().callEvent(tBanEvent);
        if (tBanEvent.isCancelled()){
            return;
        }
        senderName = tBanEvent.getSenderName();
        reason = tBanEvent.getReason();
        duration = tBanEvent.getDuration();
        measure = tBanEvent.getMeasure();

        new Thread(()-> {
            JsonHandler webHandle = new JsonHandler(plugin);
            HashMap<String, String> url_items = new HashMap<String, String>();
            url_items.put("player", playerName);
            url_items.put("player_uuid", playerUUID);
            url_items.put("playerip", playerIP);
            url_items.put("reason", reason);
            url_items.put("admin", senderName);
            url_items.put("admin_uuid", senderUUID);
            url_items.put("duration", duration);
            url_items.put("measure", measure);
            if (actionData != null) {
                url_items.put("actionData", actionData.toString());
            }
            url_items.put("exec", "tempBan");
            HashMap<String, String> response = webHandle.mainRequest(url_items);
            try {
                if (response.containsKey("error")) {
                    Util.message(senderName, ChatColor.RED + "Error: " + response.get("error"));
                    return;
                }
                if (response.containsKey("player")) {
                    playerName = response.get("player");
                }
                if (!response.containsKey("result")) {
                    //bukkitBan(); // don't use default ban
                    Util.message(senderName, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
                    return;
                }
                if (response.get("result").equals("y")) {
                    this.kickPlayer(playerName, playerUUID, localize("tempBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                    Util.broadcastMessage(ChatColor.GREEN + localize("tempBanSuccess", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));

                    Bukkit.getScheduler().runTaskLater(plugin, ()->plugin.getServer().getPluginManager().callEvent(new PlayerBanEvent(playerName, playerUUID, playerIP, senderName, senderUUID, reason, action_id, duration, measure)),1);

                    log.info(playerName + " has been banned with a temp type ban [" + reason + "] [" + senderName + "]!");
                    return;
                } else if (response.get("result").equals("e")) {
                    Util.message(senderName, ChatColor.RED + localize("tempBanError", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("s")) {
                    Util.message(senderName, ChatColor.RED + localize("tempBanGroup", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("a")) {
                    Util.message(senderName, ChatColor.RED + localize("tempBanAlready", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                } else if (response.get("result").equals("n")) {
                    if (response.get("msg") != null) {
                        Util.message(senderName, ChatColor.RED + response.get("msg"));
                    } else {
                        Util.message(senderName, ChatColor.RED + localize("tempBanError", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
                    }
                }
                log.info(senderName + " tried to ban " + playerName + " with a temp type ban [" + reason + "]!");
            } catch (Exception ex) {
                //bukkitBan();
                log.warning("Error occurred with temporary banning. Please report this to an MCBans developer.");
                ex.printStackTrace();
            }
        }).start();
    }

	private void bukkitBan(final boolean flag){
        OfflinePlayer target = plugin.getServer().getPlayer(playerName);
        if (target == null){
            return;
        }
        if (flag){
            if (!plugin.getServer().getBanList(BanList.Type.NAME).isBanned(target.getName())){
            	plugin.getServer().getBanList(BanList.Type.NAME).addBan(target.getName(), reason, new Date(), senderName);
                this.kickPlayer(playerName, playerUUID, localize("localBanPlayer", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason, I18n.IP, playerIP));
            }
        }else{
        	if (plugin.getServer().getBanList(BanList.Type.NAME).isBanned(target.getName())){
        		plugin.getServer().getBanList(BanList.Type.NAME).pardon(target.getName());
            }
        }
    }

    @SuppressWarnings("unused")
	private Map<String, JSONObject> getProof() throws JSONException{
        HashMap<String, JSONObject> ret = new HashMap<String, JSONObject>();

        /* Hacked client */
        // No catch PatternSyntaxException. This exception thrown when compiling invalid regex.
        // In this case, regex is constant string. Next line is wrong if throw this. So should output full exception message.
        Pattern regex = Pattern.compile("(fly|hack|nodus|glitch|exploit|NC|cheat|nuker|x-ray|xray)");
        boolean foundMatch = regex.matcher(reason).find();

        if (foundMatch) {
            Player p = plugin.getServer().getPlayerExact(playerName);
            if (p != null) playerName = p.getName();
        }

        return ret;
    }

    private void err() {
        log.warning("\nError: MCBans caught an invalid action. Perhaps another plugin is using MCBans improperly?\n");
    }
}
