package com.mcbans.firestar.mcbans.pluginInterface;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.Settings;
import com.mcbans.firestar.mcbans.events.PlayerKickEvent;

@SuppressWarnings("unused")
public class Kick implements Runnable {
    private Settings settings;
    private BukkitInterface plugin;
    private String playerName = null;
    private String senderName = null;
    private String reason = null;

    public Kick(Settings settings, BukkitInterface plugin, String playerName, String senderName, String reason) {
        this.settings = settings;
        this.plugin = plugin;
        this.playerName = playerName;
        this.senderName = senderName;
        this.reason = reason;
    }

    @Override
    public void run() {
        while (plugin.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }
        final Player player = plugin.getServer().getPlayer(playerName);
        if (player != null) {
            // Call PlayerKickEvent
            PlayerKickEvent kickEvent = new PlayerKickEvent(playerName, senderName, reason);
            plugin.getServer().getPluginManager().callEvent(kickEvent);
            if (kickEvent.isCancelled()){
                return;
            }
            reason = kickEvent.getReason();

            plugin.log(senderName + " has kicked " + player.getName() + " [" + reason + "]");
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(plugin.language.getFormat("kickMessagePlayer", player.getName(), senderName, reason));
                }
            }, 1L);
            plugin.broadcastAll(ChatColor.GREEN
                    + plugin.language.getFormat("kickMessageBroadcast", playerName, senderName, reason, "%ADMIN% has kicked %PLAYER% [%REASON%]",
                            true));
        } else {
            plugin.broadcastPlayer(
                    senderName,
                    ChatColor.DARK_RED
                    + plugin.language.getFormat("kickMessageNoPlayer", playerName, senderName, reason, "No player with that name online!",
                            true));
        }
    }
}