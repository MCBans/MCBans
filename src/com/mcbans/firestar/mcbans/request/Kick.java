package com.mcbans.firestar.mcbans.request;

import static com.mcbans.firestar.mcbans.I18n._;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.events.PlayerKickEvent;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.util.Util;

public class Kick implements Runnable {
    private final MCBans plugin;

    private final String playerName;
    private final String senderName;
    private String reason;
    private final boolean useExactName;

    public Kick(final MCBans plugin, final String playerName, final String senderName, final String reason, final boolean useExactName) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.senderName = senderName;
        this.reason = reason;
        this.useExactName = useExactName;
    }

    @Deprecated
    public Kick(final MCBans plugin, final String playerName, final String senderName, final String reason){
        this(plugin, playerName, senderName, reason, false);
    }

    @Override
    public void run() {
        final Player player = (useExactName) ? plugin.getServer().getPlayerExact(playerName) : plugin.getServer().getPlayer(playerName);
        if (player != null) {
            // Check exempt permission
            if (Perms.EXEMPT_KICK.has(player)){
                Util.message(senderName, ChatColor.RED + _("kickExemptPlayer", I18n.PLAYER, playerName));
                return;
            }
            
            // Call PlayerKickEvent
            PlayerKickEvent kickEvent = new PlayerKickEvent(player.getName(), senderName, reason);
            plugin.getServer().getPluginManager().callEvent(kickEvent);
            if (kickEvent.isCancelled()){
                return;
            }
            reason = kickEvent.getReason();

            // kick player
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(_("kickPlayer", I18n.PLAYER, player.getName(), I18n.SENDER, senderName, I18n.REASON, reason));
                }
            }, 0L);

            Util.broadcastMessage(ChatColor.GREEN + _("kickBroadcast", I18n.PLAYER, player.getName(), I18n.SENDER, senderName, I18n.REASON, reason));
            plugin.getLog().info(senderName + " has kicked " + player.getName() + " [" + reason + "]");
        } else {
            Util.message(senderName, ChatColor.RED + _("kickNoPlayer", I18n.PLAYER, playerName));
        }
    }
}