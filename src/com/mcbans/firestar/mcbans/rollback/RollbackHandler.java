package com.mcbans.firestar.mcbans.rollback;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import uk.co.oliwali.HawkEye.HawkEye;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;

import de.diddiz.LogBlock.LogBlock;

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
        Plugin check = pm.getPlugin("LogBlock");
        if (check != null && check instanceof LogBlock && check.isEnabled()) {
            method = new LbRollback(plugin);
            if (method.setPlugin(check)){
                plugin.log(LogLevels.INFO, "LogBlock plugin found. Using this for rollback.");
                return true;
            }
        }

        // Check HawkEye
        check = pm.getPlugin("HawkEye");
        if (check != null && check instanceof HawkEye && check.isEnabled()) {
            method = new HeRollback(plugin);
            plugin.log(LogLevels.INFO, "HawkEye plugin found. Using this for rollback.");
            return true;
        }

        // Check CoreProtect
        check = pm.getPlugin("CoreProtect");
        if (check != null && check instanceof CoreProtect && check.isEnabled()) {
            CoreProtectAPI cpAPI = ((CoreProtect) check).getAPI();
            if (cpAPI.isEnabled()){
                method = new CpRollback(plugin);
                method.setPlugin(check);
                plugin.log(LogLevels.INFO, "CoreProtect plugin found. Using this for rollback.");
                return true;
            }else{
                plugin.log(LogLevels.INFO, "CoreProtect plugin found but disabled API.");
                plugin.log(LogLevels.INFO, "Change 'api-enabled' value of CoreProtect config.yml and restart server!");
            }
        }

        plugin.log(LogLevels.INFO, "Rollback plugin not found!");
        method = null;

        return false;
    }

    /**
     * Rollback with detected rollback method
     */
    public boolean rollback(final String senderName, final String target){
        if (method == null){
            return false;
        }

        //worlds = plugin.Settings.getString("affectedWorlds").split(",");

        CommandSender sender = plugin.getServer().getPlayer(senderName);
        if (sender == null) sender = plugin.getServer().getPlayer(target);
        if (sender == null) sender = plugin.getServer().getConsoleSender();

        return method.rollback(sender, senderName, target);
    }
}
