package com.mcbans.domain.models.client;

import java.io.Serializable;
import java.util.Date;

public class Ban implements Serializable {
    private long id;
    private String reason;
    private Player admin;
    private Player player;
    private double reputation;
    private String type;
    private Long duration;
    private Server server;
    private Date date;

    public Ban() {
    }

    public Ban(String type, String reason, double reputation, Player admin) {
        this.reason = reason;
        this.reputation = reputation;
        this.type = type;
        this.admin = admin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getReputation() {
        return reputation;
    }

    public void setReputation(double reputation) {
        this.reputation = reputation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Player getAdmin() {
        return admin;
    }

    public void setAdmin(Player admin) {
        this.admin = admin;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
