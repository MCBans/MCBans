package com.mcbans.plugin.request;

import static com.mcbans.plugin.I18n.localize;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.plugin.I18n;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.events.PlayerKickEvent;
import com.mcbans.plugin.permission.Perms;
import com.mcbans.plugin.util.Util;

public class Kick {
    private final MCBans plugin;

    private final String playerName;
    private final String senderName;
    private String reason,senderUUID,playerUUID;
    private final boolean useExactName;

    public Kick(final MCBans plugin, final String playerName, final String playerUUID, final String senderName, final String senderUUID, final String reason, final boolean useExactName) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.senderName = senderName;
        this.reason = reason;
        this.useExactName = useExactName;
        this.senderUUID = senderUUID;
        this.playerUUID = playerUUID;
    }

    @Deprecated
    public Kick(final MCBans plugin, final String playerName, final String senderName, final String reason){
        this(plugin, playerName, "", senderName, "", reason, false);
    }

    public void run() {
    	Player playertmp = null;
    	if(playerUUID!=null){
    		playertmp = MCBans.getPlayer(plugin, MCBans.fromString(playerUUID));
    	}else{
    		playertmp = (useExactName) ? MCBans.getPlayer(plugin, playerName) : plugin.getServer().getPlayer(playerName);
    	}
    	final Player player = playertmp;
        if (player != null) {
            // Check exempt permission
            if (Perms.EXEMPT_KICK.has(player)){
                Util.message(senderName, ChatColor.RED + localize("kickExemptPlayer", I18n.PLAYER, player.getName()));
                return;
            }
            
            // Call PlayerKickEvent
            PlayerKickEvent kickEvent = new PlayerKickEvent(player.getName(), playerUUID, senderName, senderUUID, reason);
            plugin.getServer().getPluginManager().callEvent(kickEvent);
            if (kickEvent.isCancelled()){
                return;
            }
            reason = kickEvent.getReason();

            // kick player
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(localize("kickPlayer", I18n.PLAYER, player.getName(), I18n.SENDER, senderName, I18n.REASON, reason));
                }
            }, 0L);

            Util.broadcastMessage(ChatColor.GREEN + localize("kickSuccess", I18n.PLAYER, player.getName(), I18n.SENDER, senderName, I18n.REASON, reason));
            plugin.getLog().info(senderName + " has kicked " + player.getName() + " [" + reason + "]");
        } else {
            Util.message(senderName, ChatColor.RED + localize("kickNoPlayer", I18n.PLAYER, playerName));
        }
    }
}