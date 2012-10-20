package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerGlobalBanEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String player;
    private String playerIP;
    private String sender;
    private String reason;

    public PlayerGlobalBanEvent(String player, String playerIP, String sender, String reason) {
        this.player = player;
        this.playerIP = playerIP;
        this.sender = sender;
        this.reason = reason;
    }

    public String getPlayerName() {
        return this.player;
    }

    public String getPlayerIP() {
        return this.playerIP;
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
