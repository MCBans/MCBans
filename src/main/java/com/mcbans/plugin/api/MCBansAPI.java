package com.mcbans.plugin.api;

import java.util.HashMap;

import com.mcbans.plugin.BanType;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.AltLookupCallback;
import com.mcbans.plugin.callBacks.BanLookupCallback;
import com.mcbans.plugin.callBacks.LookupCallback;
import com.mcbans.plugin.callBacks.MessageCallback;
import com.mcbans.utils.IPTools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.mcbans.plugin.request.AltLookupRequest;
import com.mcbans.plugin.request.Ban;
import com.mcbans.plugin.request.BanIpRequest;
import com.mcbans.plugin.request.BanLookupRequest;
import com.mcbans.plugin.request.Kick;
import com.mcbans.plugin.request.LookupRequest;
import com.mcbans.plugin.util.Util;

public class MCBansAPI {
    private final MCBans plugin;
    private final String pname;

    private MCBansAPI(final MCBans plugin, final String pname) {
        plugin.getLog().info("MCBans API linked with: " + pname);
        this.plugin = plugin;
        this.pname = pname;
    }

    private void ban(BanType type, String targetName, String targetUUID, String senderName, String senderUUID, String reason, String duration, String measure){
        // check null
        if (targetName == null || senderName == null){
            return;
        }

        String targetIP = "";
        if (type != BanType.UNBAN){
            final Player target = Bukkit.getPlayerExact(targetName);
            targetIP = (target != null) ? target.getAddress().getAddress().getHostAddress() : "";
        }

        Ban banControl = new Ban(plugin, type.getActionName(), targetName, targetUUID, targetIP, senderName, senderUUID, reason, duration, measure, null, false);
        banControl.run();
    }

    /**
     * Add Locally BAN
     * @param targetName BAN target player's name
     * @param senderName BAN issued admin's name
     * @param reason BAN reason
     */
    public void localBan(String targetName, String targetUUID, String senderName, String senderUUID, String reason){
        plugin.getLog().info("Plugin " + pname + " tried to local ban player " + targetName);

        reason = (reason == null || reason == "") ? plugin.getConfigs().getDefaultLocal() : reason;
        this.ban(BanType.LOCAL, targetName, targetUUID, senderName, senderUUID, reason, "", "");
    }

    /**
     * Add Globally BAN
     * @param targetName BAN target player's name
     * @param senderName BAN issued admin's name
     * @param reason BAN reason
     */
    public void globalBan(String targetName, String targetUUID, String senderName, String senderUUID, String reason){
        plugin.getLog().info("Plugin " + pname + " tried to global ban player " + targetName);
        if (reason == null || reason == "") return;
        this.ban(BanType.GLOBAL, targetName, targetUUID, senderName, senderUUID, reason, "", "");
    }

    /**
     * Add Temporary BAN
     * @param targetName BAN target player's name
     * @param senderUUID BAN issued admin's UUID
     * @param senderName BAN issued admin's name
     * @param senderUUID BAN issued admin's UUID
     * @param reason BAN reason
     * @param duration Banning length duration (intValue)
     * @param measure Banning length measure (m(minute), h(hour), d(day), w(week))
     */
    public void tempBan(String targetName, String targetUUID, String senderName, String senderUUID, String reason, String duration, String measure){
        plugin.getLog().info("Plugin " + pname + " tried to temp ban player " + targetName);

        reason = (reason == null || reason == "") ? plugin.getConfigs().getDefaultTemp() : reason;
        duration = (duration == null) ? "" : duration;
        measure = (measure == null) ? "" : measure;
        this.ban(BanType.TEMP, targetName, targetUUID, senderName, senderUUID, reason, duration, measure);
    }

    /**
     * Remove BAN
     * @param targetName UnBan target player's name
     * @param senderName UnBan issued admin's name
     * @param senderUUID UnBan issued admin's UUID
     */
    public void unBan(String targetName, String targetUUID, String senderName, String senderUUID){
        plugin.getLog().info("Plugin " + pname + " tried to unban player " + targetName);
        if (targetName == null || senderName == null){
            plugin.getLog().info("Invalid usage (null): unBan");
            return;
        }
        if (!Util.isValidName(targetName) && !IPTools.validBanIP(targetName)){
            plugin.getLog().info("The target you are trying to unban is not a valid name or IP format!");
            return;
        }

        this.ban(BanType.UNBAN, targetName, targetUUID, senderName, senderUUID, "", "", "");
    }
    
    /**
     * Add IPBan
     * @param ip target ip address
     * @param senderName IPBan issued admin's name
     * @param senderUUID IPBan issued admin's UUID
     * @param reason Ban reason
     * @param callback MessageCallback
     */
    public void ipBan(String ip, String senderName, String senderUUID, String reason, MessageCallback callback){
        plugin.getLog().info("Plugin " + pname + " tried to ip ban " + ip);
        if (ip == null || senderName == null || callback == null){
            plugin.getLog().info("Invalid usage (null): ipBan");
            return;
        }
        if (!IPTools.validBanIP(ip)){
            plugin.getLog().info("Invalid IP address (" + ip + "): ipBan");
            return;
        }
        
        if (reason == null || reason.length() <= 0){
            reason = plugin.getConfigs().getDefaultLocal();
        }

        BanIpRequest request = new BanIpRequest(plugin, callback, ip, reason, senderName, senderUUID);
        Thread thread = new Thread(request);
        thread.start();
    }
    
    /**
     * Add IPBan
     * @param ip target ip address
     * @param senderName IPBan issued admin's name
     * @param reason Ban reason
     */
    public void ipBan(String ip, String senderName, String reason){
        this.ipBan(ip, senderName, "", reason, new MessageCallback(plugin));
    }

    /**
     * Kick Player
     * @param targetName Kick target player's name
     * @param senderName Kick issued admin's name
     * @param reason Kick reason
     */
    public void kick(String targetName, String targetUUID, String senderName, String senderUUID, String reason){
        //plugin.getLog().info("Plugin " + pname + " tried to kick player " + targetName);
        reason = (reason == null || reason == "") ? plugin.getConfigs().getDefaultKick() : reason;

        // Start
        Kick kickPlayer = new Kick(plugin, targetName, targetUUID, senderName, senderUUID, reason, true);
        kickPlayer.run();
    }

    /**
     * Lookup Player
     * @param targetName Lookup target player name
     * @param senderName Lookup issued admin name
     * @param senderUUID Lookup issued admin UUID
     * @param callback LookupCallback
     */
    public void lookupPlayer(String targetName, String targetUUID, String senderName, String senderUUID, LookupCallback callback){
        plugin.getLog().info("Plugin " + pname + " tried to lookup player " + targetName);
        if (targetName == null || callback == null){
            plugin.getLog().info("Invalid usage (null): lookupPlayer");
            return;
        }

        if (!Util.isValidName(targetName)){
            callback.error("Invalid lookup target name.");
        }

        LookupRequest request = new LookupRequest(plugin, callback, targetName, targetUUID, senderName, senderUUID);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    /**
     * Lookup Ban
     * @param banID Lookup target ban ID
     * @param callback BanLookupCallback
     */
    public void lookupBan(int banID, BanLookupCallback callback){
        plugin.getLog().info("Plugin " + pname + " tried to ban lookup " + banID);
        if (banID < 0 || callback == null){
            plugin.getLog().info("Invalid usage (null): lookupBan");
            return;
        }

        BanLookupRequest request = new BanLookupRequest(plugin, callback, banID);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }
    
    /**
     * Lookup Alt Accounts
     * @param playerName Lookup target player name
     * @param callback BanLookupCallback
     */
    public void lookupAlt(String playerName, AltLookupCallback callback){
        plugin.getLog().info("Plugin " + pname + " tried to alt lookup " + playerName);
        if (playerName == null || callback == null){
            plugin.getLog().info("Invalid usage (null): lookupAlt");
            return;
        }
        
        if (!Util.isValidName(playerName)){
            callback.error("Invalid alternate account lookup target name.");
        }

        AltLookupRequest request = new AltLookupRequest(plugin, callback, playerName);
        Thread triggerThread = new Thread(request);
        triggerThread.start();
    }

    /**
     * Get MCBans plugin version
     * @return plugin version
     */
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    private static HashMap<Plugin, MCBansAPI> apiHandles = new HashMap<Plugin, MCBansAPI>();
    public static MCBansAPI getHandle(final MCBans plugin, final Plugin otherPlugin){
        if (otherPlugin == null) return null;

        MCBansAPI api = apiHandles.get(otherPlugin);

        if (api == null){
            // get new api
            api = new MCBansAPI(plugin, otherPlugin.getName());

            apiHandles.put(otherPlugin, api);
        }

        return api;
    }
}
