package com.mcbans.client;

public enum MCBansCommands {
    //Session Handling
    SessionRegister(127),
    SessionClose(-127),

    // Ban Information
    BanStatusByPlayerName(10),
    BanStatusByPlayerUUID(11),

    // Ban User
    BanPlayerByUUID(20);
    private final int value;
    MCBansCommands(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte)value;
    }
}
