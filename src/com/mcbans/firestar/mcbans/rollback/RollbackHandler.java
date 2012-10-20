package com.mcbans.firestar.mcbans.rollback;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import uk.co.oliwali.HawkEye.HawkEye;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;

public class RollbackHandler {
    private final BukkitInterface plugin;
    private RollbackMethod method = null;

    // plugins
    private LogBlock logblock = null;
    private HawkEye hawkeye = null;

    // rollback setting
    String[] worlds;

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
        if (check != null) {
            logblock = (LogBlock) check;
            method = RollbackMethod.LOGBLOCK;
            plugin.log(LogLevels.INFO, "LogBlock plugin found. Using this for rollback.");
            return true;
        }

        // Check HawkEye
        check = pm.getPlugin("HawkEye");
        if (check != null) {
            hawkeye = (HawkEye) check;
            method = RollbackMethod.HAWKEYE;
            plugin.log(LogLevels.INFO, "HawkEye plugin found. Using this for rollback.");
            return true;
        }

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

        worlds = plugin.Settings.getString("affectedWorlds").split(",");

        switch (method) {
            case LOGBLOCK:
                return rb_logblock(admin, target, time);
            case HAWKEYE:
                return rb_hawkeye(admin, target, time);
        }
        return false;
    }

    /**
     * Rollback with LogBlock
     */
    private boolean rb_logblock(final String admin, final String target, final int time){
        CommandSender sender = plugin.getServer().getPlayer(admin);
        if (sender == null) {
            sender = plugin.getServer().getPlayer(target);
        }
        if (sender == null) {
            // TODO: Not tested. Remove it if LogBlock don't accept ConsoleCommandSender.
            sender = Bukkit.getConsoleSender();
        }

        if (sender != null) {
            for (String world : worlds) {
                QueryParams params = new QueryParams(this.logblock);
                params.setPlayer(target);
                params.since = (time * plugin.Settings.getInteger("backDaysAgo"));
                params.world = plugin.getServer().getWorld(world);
                params.silent = false;
                try {
                    this.logblock.getCommandsHandler().new CommandRollback(sender, params, true);
                    plugin.broadcastPlayer(admin, ChatColor.GREEN + "Rollback successful!");
                } catch (Exception e) {
                    plugin.broadcastPlayer(admin, ChatColor.RED + "Unable to rollback player!");
                    if (plugin.Settings.getBoolean("isDebug")) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            plugin.log(admin + " has tried to rollback " + target
                    + " but neither were online, so rollback was ignored (run this command seperately!)!");
        }

        return true;
    }

    /**
     * Rollback with HawkEye
     */
    private boolean rb_hawkeye(final String admin, final String target, final int time){
        return false;
    }
}
