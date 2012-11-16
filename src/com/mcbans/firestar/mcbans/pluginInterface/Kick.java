package com.mcbans.firestar.mcbans.pluginInterface;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.events.PlayerKickEvent;
import com.mcbans.firestar.mcbans.util.Util;
import static com.mcbans.firestar.mcbans.I18n._;

@SuppressWarnings("unused")
public class Kick implements Runnable {
    private MCBans plugin;
    private String playerName = null;
    private String senderName = null;
    private String reason = null;

    public Kick(MCBans plugin, String playerName, String senderName, String reason) {
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

            plugin.getLog().info(senderName + " has kicked " + player.getName() + " [" + reason + "]");
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(_("kickMessagePlayer").replaceAll(I18n.PLAYER, player.getName()).replaceAll(I18n.SENDER, senderName).replaceAll(I18n.REASON, reason));
                }
            }, 1L);
            Util.broadcastMessage(ChatColor.GREEN
                    + _("kickMessageBroadcast").replaceAll(I18n.PLAYER, playerName).replaceAll(I18n.SENDER, senderName).replaceAll(I18n.REASON, reason));
        } else {
            Util.message(senderName, ChatColor.DARK_RED + _("kickMessageNoPlayer").replaceAll(I18n.PLAYER, playerName));
        }
    }
}