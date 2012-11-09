package com.mcbans.firestar.mcbans.rollback;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.MCBans;

public abstract class BaseRollback {
    protected final MCBans plugin;

    // rollback setting
    String[] worlds;

    public BaseRollback(final MCBans plugin){
        this.plugin = plugin;
        worlds = plugin.getConfigs().getAffectedWorlds().split(",");
    }

    public abstract boolean rollback(final CommandSender sender, final String senderName, final String target);

    public boolean setPlugin(final Plugin plugin){
        return true;
    }
}
