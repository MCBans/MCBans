package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerBannedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private String player;
    private String playerUUID;
    private String playerIP;
    private String sender;
    private String senderUUID;
    private String reason;
    private int action_id;
    private String duration;
    private String measure;

    public PlayerBannedEvent(String player, String playerUUID, String playerIP, String sender, String senderUUID, String reason, int action_id, String duration, String measure) {
        this.player = player;
        this.playerIP = playerIP;
        this.playerUUID = playerUUID;
        this.sender = sender;
        this.reason = reason;
        this.action_id = action_id;
        this.duration = duration;
        this.measure = measure;
        this.senderUUID = senderUUID;
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

    public String getReason() {
        return this.reason;
    }

    public String getDuration() {
        return this.duration;
    }

    public String getMeasure() {
        return this.measure;
    }

    public boolean isGlobalBan() {
        return (this.action_id == 0);
    }

    public boolean isLocalBan() {
        return (this.action_id == 1);
    }

    public boolean isTempBan() {
        return (this.action_id == 2);
    }

    public UUID getPlayerUUID() {
        return UUID.fromString(this.playerUUID);
    }

    public UUID getSenderUUID() {
        return UUID.fromString(this.senderUUID);
    }

    public String getPlayerUUIDAsString() {
        return this.playerUUID;
    }

    public String getSenderUUIDAsString() {
        return this.senderUUID;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
