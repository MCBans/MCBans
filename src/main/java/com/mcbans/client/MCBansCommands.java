package com.mcbans.client;

public enum MCBansCommands {
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

    // Player Inventory
    GetPlayerInventory(50),
    SavePlayerInventory(51),

    // Sync bans
    BanSync(25),

    // Information Callback
    InformationCallback(40),

    // Unban commands
    UnBanPlayer(28),
    UnBanIp(29);

    private final int value;
    MCBansCommands(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte)value;
    }
}
