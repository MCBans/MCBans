package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUnbanEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String player;
    private String sender;

    public PlayerUnbanEvent(String player, String sender) {
        this.player = player;
        this.sender = sender;
    }

    public String getPlayerName() {
        return this.player;
    }

    public String getSenderName() {
        return this.sender;
    }

    public void setSenderName(String senderName) {
        this.sender = senderName;
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
