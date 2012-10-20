package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUnbannedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private String player;
    private String sender;

    public PlayerUnbannedEvent(String player, String sender) {
        this.player = player;
        this.sender = sender;
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
