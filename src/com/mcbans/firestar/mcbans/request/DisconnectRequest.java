package com.mcbans.firestar.mcbans.request;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.callBacks.MessageCallback;

public class DisconnectRequest extends BaseRequest<MessageCallback>{
	
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
