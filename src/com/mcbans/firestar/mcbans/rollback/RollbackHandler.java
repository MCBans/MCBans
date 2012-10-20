package com.mcbans.firestar.mcbans.rollback;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;

public class RollbackHandler {
    private final BukkitInterface plugin;
    private BaseRollback method = null;

    public RollbackHandler(final BukkitInterface plugin){
        this.plugin = plugin;
    }

    /**
     * Setup and select rollback method
     * @return true if integration plugin found
     */
    public boolean setupHandler(){
        PluginManager pm = plugin.getServer().getPluginManager();

        // Check LogBlock
        Plugin checkLb = pm.getPlugin("LogBlock");
        if (checkLb != null && checkLb.isEnabled()) {
            method = new LbRollback(plugin);
            plugin.log(LogLevels.INFO, "LogBlock plugin found. Using this for rollback.");
            return true;
        }

        // Check HawkEye
        Plugin checkHe = pm.getPlugin("HawkEye");
        if (checkHe != null && checkHe.isEnabled()) {
            method = new HeRollback(plugin);
            plugin.log(LogLevels.INFO, "HawkEye plugin found. Using this for rollback.");
            return true;
        }

        plugin.log(LogLevels.INFO, "Rollback plugin not found!");
        method = null;

        return false;
    }

    /**
     * Rollback with detected rollback method
     */
    public boolean rollback(final String admin, final String target, final int time){
        if (method == null){
            return false;
        }

        //worlds = plugin.Settings.getString("affectedWorlds").split(",");

        CommandSender sender = plugin.getServer().getPlayer(admin);
        if (sender == null) sender = plugin.getServer().getPlayer(target);
        if (sender == null) sender = plugin.getServer().getConsoleSender();

        return method.rollback(sender, admin, target, time);
    }
}
