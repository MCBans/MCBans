package com.mcbans.firestar.mcbans.pluginInterface;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.Settings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Kick implements Runnable {
    private Settings Config;
    private BukkitInterface MCBans;
    private String PlayerName = null;
    private String PlayerAdmin = null;
    private String Reason = null;

    public Kick(Settings cf, BukkitInterface p, String playerName, String playerAdmin, String reason) {
        Config = cf;
        MCBans = p;
        PlayerName = playerName;
        PlayerAdmin = playerAdmin;
        Reason = reason;
    }

    @Override
    public void run() {
        while (MCBans.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }
        final Player player = MCBans.getServer().getPlayer(PlayerName);
        if (player != null) {
            MCBans.log(PlayerAdmin + " has kicked " + player.getName() + " [" + Reason + "]");
            MCBans.getServer().getScheduler().scheduleSyncDelayedTask(MCBans, new Runnable() {
                public void run() {
                    player.kickPlayer(MCBans.Language.getFormat("kickMessagePlayer", player.getName(), PlayerAdmin, Reason));
                }
            }, 1L);
            MCBans.broadcastKickView(ChatColor.GREEN
                    + MCBans.Language.getFormat("kickMessageBroadcast", PlayerName, PlayerAdmin, Reason, "%ADMIN% has kicked %PLAYER% [%REASON%]",
                            true));
        } else {
            MCBans.broadcastPlayer(
                    PlayerAdmin,
                    ChatColor.DARK_RED
                            + MCBans.Language.getFormat("kickMessageNoPlayer", PlayerName, PlayerAdmin, Reason, "No player with that name online!",
                                    true));
        }
    }
}