package com.mcbans.plugin.request;

import java.util.UUID;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.MessageCallback;

public class DisconnectRequest extends BaseRequest<MessageCallback>{
    private long startTime;

    public DisconnectRequest(final MCBans plugin, final String playerName, final UUID playerUUID){
        super(plugin, new MessageCallback(plugin, null));

        this.items.put("player", playerName);
        this.items.put("player_uuid", playerUUID.toString());
        this.items.put("exec", "playerDisconnect");
    }
    public DisconnectRequest(final MCBans plugin, final String playerName){
        super(plugin, new MessageCallback(plugin, null));

        this.items.put("player", playerName);
        this.items.put("exec", "playerDisconnect");
    }

    @Override
    protected void execute() {
        request();
    }
}
