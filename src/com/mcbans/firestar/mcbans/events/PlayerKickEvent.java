package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKickEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String player;
    private String sender;
    private String reason;

    public PlayerKickEvent(String player, String sender, String reason) {
        this.player = player;
        this.sender = sender;
        this.reason = reason;
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
