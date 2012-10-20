package com.mcbans.firestar.mcbans.rollback;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.BukkitInterface;

public abstract class BaseRollback {
    protected final BukkitInterface plugin;

    // rollback setting
    String[] worlds;

    public BaseRollback(final BukkitInterface plugin){
        this.plugin = plugin;
        worlds = plugin.Settings.getString("affectedWorlds").split(",");
    }

    public abstract boolean rollback(final CommandSender sender, final String admin, final String target);

    public boolean setPlugin(final Plugin plugin){
        return true;
    }
}
