package com.mcbans.firestar.mcbans.events;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcbans.firestar.mcbans.MCBans;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BungeeCordEventListener implements Listener {
    
    private final MCBans instance;

    public BungeeCordEventListener(final MCBans plugin) {
        instance = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBan(PlayerBannedEvent event) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("KickPlayer");
        out.writeUTF(event.getPlayerName());
        out.writeUTF(event.getReason());
        Bukkit.getPlayer(event.getPlayerName()).sendPluginMessage(instance, "BungeeCord", out.toByteArray());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("KickPlayer");
        out.writeUTF(event.getPlayer());
        out.writeUTF(event.getReason());
        Bukkit.getPlayer(event.getPlayer()).sendPluginMessage(instance, "BungeeCord", out.toByteArray());
    }
    
}
