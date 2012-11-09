package com.mcbans.firestar.mcbans.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.BanType;
import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.Ban;

public class MCBansAPI {
    private final BukkitInterface plugin;

    public MCBansAPI(final BukkitInterface plugin) {
        this.plugin = plugin;
    }

    private void Ban(BanType type, String targetName, String senderName, String reason, String duration, String measure){
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

    public void LocalBan(String targetName, String senderName, String reason){
        reason = (reason == null || reason == "") ? plugin.settings.getString("defaultLocal") : reason;
        this.Ban(BanType.LOCAL, targetName, senderName, reason, "", "");
    }

    public void GlobalBan(String targetName, String senderName, String reason){
        if (reason == null || reason == "") return;
        this.Ban(BanType.GLOBAL, targetName, senderName, reason, "", "");
    }

    public void TempBan(String targetName, String senderName, String reason, String duration, String measure){
        reason = (reason == null || reason == "") ? plugin.settings.getString("defaultTemp") : reason;
        duration = (duration == null) ? "" : duration;
        measure = (measure == null) ? "" : measure;
        this.Ban(BanType.TEMP, targetName, senderName, reason, duration, measure);
    }

    public void UnBan(String targetName, String senderName){
        this.Ban(BanType.UNBAN, targetName, senderName, "", "", "");
    }
}
