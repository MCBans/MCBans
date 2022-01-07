package com.mcbans.domain;

import java.io.Serializable;
import java.util.Date;

public class Ban implements Serializable {
    private long id;
    private String reason;
    private String adminName;
    private String adminUUID;
    private String serverAddress;
    private double reputation;
    private String type;
    private Long duration;
    private Long server;
    private Date date;

    public Ban() {
    }

    public Ban(String type, String reason, double reputation, String adminName) {
        this.reason = reason;
        this.reputation = reputation;
        this.type = type;
        this.adminName = adminName;
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

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
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

    public Long getServer() {
        return server;
    }

    public void setServer(Long server) {
        this.server = server;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminUUID() {
        return adminUUID;
    }

    public void setAdminUUID(String adminUUID) {
        this.adminUUID = adminUUID;
    }
}
