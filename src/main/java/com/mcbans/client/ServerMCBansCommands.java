package com.mcbans.client;

public enum ServerMCBansCommands {
    //Session Handling
    SessionRegister(127),
    VerifyConnection(126),
    SessionClose(-127),

    // Ban Information
    BanStatusByPlayerName(10),
    BanStatusByPlayerUUID(11),

    // Ban User
    BanPlayer(20),
    BanIp(21),

    // Sync bans
    BanSync(25),

    // Unban commands
    UnBanPlayer(28),
    UnBanIp(29),

    // Information Callback
    InformationCallback(40),

    // Player Inventory
    GetPlayerInventory(50),
    SavePlayerInventory(51),

    // Pending actions
    PendingActions(60),

    // lookups
    PlayerLookup(70),
    BanLookup(71);


    private final int value;
    ServerMCBansCommands(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte)value;
    }
}
