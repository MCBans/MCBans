package com.mcbans.plugin.request;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.MessageCallback;

public class DisconnectRequest extends BaseRequest<MessageCallback>{
    private long startTime;

    public DisconnectRequest(final MCBans plugin, final String playerName){
        super(plugin, new MessageCallback(plugin, null));
    }

    @Override
    protected void execute() {

    }
}
