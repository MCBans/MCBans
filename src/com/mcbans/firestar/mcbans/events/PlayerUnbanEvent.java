package com.mcbans.firestar.mcbans.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.mcbans.firestar.mcbans.util.Util;

public class PlayerUnbanEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;

    private String target,targetUUID;
    private String sender,senderUUID;

    public PlayerUnbanEvent(String target, String targetUUID, String sender, String senderUUID) {
        this.target = target;
        this.sender = sender;
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
    }
    
    public UUID getSenderUUID() {
		return UUID.fromString(senderUUID);
	}

    public String getTargetName() {
        return this.target;
    }
    
    @Deprecated
    public String getPlayerName() {
        return this.getTargetName();
    }

    public String getSenderName() {
        return this.sender;
    }

    public void setSenderName(String senderName) {
        this.sender = senderName;
    }
    
    public boolean isIPBan(){
        return Util.isValidIP(this.target);
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
