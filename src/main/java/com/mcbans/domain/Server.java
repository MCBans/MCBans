package com.mcbans.domain;

import java.io.Serializable;

public class Server implements Serializable{
    private String address;
    private double reputation;

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
}
