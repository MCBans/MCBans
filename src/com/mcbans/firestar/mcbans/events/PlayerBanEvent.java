package com.mcbans.firestar.mcbans.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    private int action_id;
    private String duration, measure, reason, sender, playerIP, player, playerUUID, senderUUID;

    public PlayerBanEvent(String player, String playerUUID, String playerIP, String sender, String senderUUID, String reason, int action_id, String duration, String measure) {
        this.player = player;
        this.playerIP = playerIP;
        this.sender = sender;
        this.reason = reason;
        this.action_id = action_id;
        this.duration = duration;
        this.measure = measure;
        this.playerUUID = playerUUID;
        this.senderUUID = senderUUID;
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

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMeasure() {
        return this.measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public boolean isGlobalBan() {
        return (this.action_id == 0);
    }

    public void setGlobalBan() {
        this.action_id = 0;
    }

    public boolean isLocalBan() {
        return (this.action_id == 1);
    }

    public void setLocalBan() {
        this.action_id = 1;
    }

    public boolean isTempBan() {
        return (this.action_id == 2);
    }

    public void setTempBan() {
        this.action_id = 2;
    }

    public int getActionID() {
        return this.action_id;
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
