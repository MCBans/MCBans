package com.mcbans.firestar.mcbans.callBacks;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class MessageCallback extends BaseCallback{
    private String message;
    private String bmessage;

    public MessageCallback(final MCBans plugin, final CommandSender sender){
        super(plugin, sender);
    }
    public MessageCallback(final MCBans plugin) {
        super(plugin, null);
    }

    public void setMessage(final String message){
        this.message = message;
    }
    public void setBroadcastMessage(final String message){
        this.bmessage = message;
    }

    @Override
    public void success() {
        if (message != null && sender != null){
            Util.message(sender, message);
        }
        if (bmessage != null && bmessage.length() > 0){
            Util.broadcastMessage(bmessage);
        }
    }

    @Override
    public void error(String error) {
        if (error != null && sender != null){
            Util.message(sender, error);
        }
    }
}
