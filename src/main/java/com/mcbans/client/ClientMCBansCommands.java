package com.mcbans.client;

public enum ClientMCBansCommands {
    UnbanSync(1),

    // stop listening for changes
    END(127);

    private final int value;
    ClientMCBansCommands(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte)value;
    }

    public static ClientMCBansCommands get(byte command){
        for(ClientMCBansCommands c: ClientMCBansCommands.values()){
            if(c.value==command)
                return c;
        }
        return null;
    }
}