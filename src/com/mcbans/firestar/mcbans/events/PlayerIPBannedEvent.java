package com.mcbans.firestar.mcbans.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerIPBannedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private String ip;
    private String sender;
    private String reason, senderUUID;

    public PlayerIPBannedEvent(String ip, String sender, String senderUUID, String reason) {
        this.ip = ip;
        this.sender = sender;
        this.reason = reason;
        this.senderUUID = senderUUID;
    }

    public UUID getSenderUUID() {
		return UUID.fromString(senderUUID);
	}
    
    public String getIP() {
        return this.ip;
    }

    public String getSenderName() {
        return this.sender;
    }

    public String getReason() {
        return this.reason;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
