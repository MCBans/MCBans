package com.mcbans.firestar.mcbans.pluginInterface;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.events.PlayerKickEvent;
import com.mcbans.firestar.mcbans.util.Util;

public class Kick implements Runnable {
    private final MCBans plugin;

    private final String playerName;
    private final String senderName;
    private String reason;

    public Kick(MCBans plugin, String playerName, String senderName, String reason) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.senderName = senderName;
        this.reason = reason;
    }

    @Override
    public void run() {
        while (plugin.apiServer == null) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
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

            plugin.getLog().info(senderName + " has kicked " + player.getName() + " [" + reason + "]");
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(_("kickMessagePlayer", I18n.PLAYER, player.getName(), I18n.SENDER, senderName, I18n.REASON, reason));
                }
            }, 1L);
            Util.broadcastMessage(ChatColor.GREEN + _("kickMessageBroadcast", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason));
        } else {
            Util.message(senderName, ChatColor.DARK_RED + _("kickMessageNoPlayer", I18n.PLAYER, playerName));
        }
    }
}