package com.mcbans.firestar.mcbans.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerIPBanEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String ip;
    private String sender;
    private String reason, senderUUID;

    public PlayerIPBanEvent(String ip, String sender, String senderUUID, String reason) {
        this.ip = ip;
        this.sender = sender;
        this.senderUUID = senderUUID;
        this.reason = reason;
    }
    
    public UUID getSenderUUID() {
		return UUID.fromString(senderUUID);
	}
    
    public String getIP(){
        return this.ip;
    }
    
    public String getSenderName() {
        return this.sender;
    }

    public void setSenderName(String senderName) {
        this.sender = senderName;
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
