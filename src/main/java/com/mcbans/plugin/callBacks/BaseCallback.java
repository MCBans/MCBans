package com.mcbans.plugin.callBacks;

import org.bukkit.command.CommandSender;

import com.mcbans.plugin.MCBans;

public abstract class BaseCallback{
    protected final MCBans plugin;
    protected final CommandSender sender;

    public BaseCallback(final MCBans plugin, final CommandSender sender){
        this.plugin = plugin;
        this.sender = sender;
    }

    public abstract void success();

    public abstract void error(final String error);

    public CommandSender getSender(){
        return this.sender;
    }

	public void success(String identifier, String playerlist) {}
}
