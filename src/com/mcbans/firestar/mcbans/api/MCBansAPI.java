package com.mcbans.firestar.mcbans.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;
import com.mcbans.firestar.mcbans.pluginInterface.Kick;

public class MCBansAPI {
    private final BukkitInterface plugin;

    public MCBansAPI(final BukkitInterface plugin) {
        this.plugin = plugin;
    }

    private void ban(BanType type, String targetName, String senderName, String reason, String duration, String measure){
        // check null
        if (targetName == null || senderName == null){
            return;
        }

        String targetIP = "";
        if (type != BanType.UNBAN){
            final Player target = Bukkit.getPlayerExact(targetName);
            targetIP = (target != null) ? target.getAddress().getAddress().getHostAddress() : "";
        }

        Ban banControl = new Ban(plugin, type.getActionName(), targetName, targetIP, senderName, reason, duration, measure);
        Thread triggerThread = new Thread(banControl);
        triggerThread.start();
    }

    public void localBan(String targetName, String senderName, String reason){
        reason = (reason == null || reason == "") ? plugin.settings.getString("defaultLocal") : reason;
        this.ban(BanType.LOCAL, targetName, senderName, reason, "", "");
    }

    public void globalBan(String targetName, String senderName, String reason){
        if (reason == null || reason == "") return;
        this.ban(BanType.GLOBAL, targetName, senderName, reason, "", "");
    }

    public void tempBan(String targetName, String senderName, String reason, String duration, String measure){
        reason = (reason == null || reason == "") ? plugin.settings.getString("defaultTemp") : reason;
        duration = (duration == null) ? "" : duration;
        measure = (measure == null) ? "" : measure;
        this.ban(BanType.TEMP, targetName, senderName, reason, duration, measure);
    }

    public void unBan(String targetName, String senderName){
        this.ban(BanType.UNBAN, targetName, senderName, "", "", "");
    }

    public void kick(String targetName, String senderName, String reason){
        reason = (reason == null || reason == "") ? plugin.settings.getString("defaultKick") : reason;

        // Start
        Kick kickPlayer = new Kick(plugin.settings, plugin, targetName, senderName, reason);
        Thread triggerThread = new Thread(kickPlayer);
        triggerThread.start();
    }
}
