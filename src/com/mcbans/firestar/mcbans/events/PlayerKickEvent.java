package com.mcbans.firestar.mcbans.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKickEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String player;
    private String sender;
    private String reason, playerUUID, senderUUID;

    public PlayerKickEvent(String player, String playerUUID, String sender, String senderUUID, String reason) {
        this.player = player;
        this.sender = sender;
        this.reason = reason;
        this.playerUUID = playerUUID;
        this.senderUUID = senderUUID;
    }

    public UUID getPlayerUUID() {
		return UUID.fromString(playerUUID);
	}

	public UUID getSenderUUID() {
		return UUID.fromString(senderUUID);
	}
    
    public String getPlayer() {
        return this.player;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
