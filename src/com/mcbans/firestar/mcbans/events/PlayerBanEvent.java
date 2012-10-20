package com.mcbans.firestar.mcbans.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBanEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String player;
    private String playerIP;
    private String sender;
    private String reason;
    private int action_id;
    private String duration;
    private String measure;

    public PlayerBanEvent(String player, String playerIP, String sender, String reason, int action_id, String duration, String measure) {
        this.player = player;
        this.playerIP = playerIP;
        this.sender = sender;
        this.reason = reason;
        this.action_id = action_id;
        this.duration = duration;
        this.measure = measure;
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
