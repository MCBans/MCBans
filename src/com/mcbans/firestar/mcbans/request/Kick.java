package com.mcbans.firestar.mcbans.request;

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

    public Kick(final MCBans plugin, final String playerName, final String senderName, final String reason) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.senderName = senderName;
        this.reason = reason;
    }

    @Override
    public void run() {
        final Player player = plugin.getServer().getPlayer(playerName);
        if (player != null) {
            // Call PlayerKickEvent
            PlayerKickEvent kickEvent = new PlayerKickEvent(playerName, senderName, reason);
            plugin.getServer().getPluginManager().callEvent(kickEvent);
            if (kickEvent.isCancelled()){
                return;
            }
            reason = kickEvent.getReason();

            // kick player
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(_("kickMessagePlayer", I18n.PLAYER, player.getName(), I18n.SENDER, senderName, I18n.REASON, reason));
                }
            }, 0L);

            Util.broadcastMessage(ChatColor.GREEN + _("kickMessageBroadcast", I18n.PLAYER, playerName, I18n.SENDER, senderName, I18n.REASON, reason));
            plugin.getLog().info(senderName + " has kicked " + player.getName() + " [" + reason + "]");
        } else {
            Util.message(senderName, ChatColor.DARK_RED + _("kickMessageNoPlayer", I18n.PLAYER, playerName));
        }
    }
}