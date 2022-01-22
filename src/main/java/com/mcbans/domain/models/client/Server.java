package com.mcbans.domain.models.client;

import java.io.Serializable;

public class Server implements Serializable{
    private long id;
    private String address;
    private double reputation;

    public Server() {
    }

    public Server(String address, double reputation) {
        this.address = address;
        this.reputation = reputation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getReputation() {
        return reputation;
    }

    public void setReputation(double reputation) {
        this.reputation = reputation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
