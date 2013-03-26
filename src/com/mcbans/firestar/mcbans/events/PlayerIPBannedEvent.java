package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerIPBannedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private String ip;
    private String sender;
    private String reason;

    public PlayerIPBannedEvent(String ip, String sender, String reason) {
        this.ip = ip;
        this.sender = sender;
        this.reason = reason;
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
