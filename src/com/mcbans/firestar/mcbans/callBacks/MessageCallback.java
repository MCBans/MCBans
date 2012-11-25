package com.mcbans.firestar.mcbans.callBacks;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class MessageCallback extends BaseCallback{
    private String message;

    public MessageCallback(final MCBans plugin, final CommandSender sender){
        super(plugin, sender);
    }

    public void setMessage(final String message){
        this.message = message;
    }

    @Override
    public void success() {
        if (message != null && sender != null){
            Util.message(sender, message);
        }
    }

    @Override
    public void error(String error) {
        if (error != null && sender != null){
            Util.message(sender, error);
        }
    }
}
