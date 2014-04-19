package com.mcbans.firestar.mcbans.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUnbannedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private String player;
    private String sender, senderUUID, playerUUID;

    public PlayerUnbannedEvent(String player, String playerUUID, String sender, String senderUUID) {
        this.player = player;
        this.sender = sender;
        this.senderUUID = senderUUID;
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
		return UUID.fromString(playerUUID);
	}

	public UUID getSenderUUID() {
		return UUID.fromString(senderUUID);
	}
    
    public String getPlayerName() {
        return this.player;
    }

    public String getSenderName() {
        return this.sender;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
