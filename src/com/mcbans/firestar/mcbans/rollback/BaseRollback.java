/**
 * mcbans3 - Package: com.mcbans.firestar.mcbans.rollback
 * Created: 2012/10/20 22:44:16
 */
package com.mcbans.firestar.mcbans.rollback;

import org.bukkit.command.CommandSender;

import com.mcbans.firestar.mcbans.BukkitInterface;

/**
 * Rollback (Rollback.java)
 * @author syam(syamn)
 */
public abstract class BaseRollback {
    protected final BukkitInterface plugin;

    // rollback setting
    String[] worlds;

    public BaseRollback(final BukkitInterface plugin){
        this.plugin = plugin;
        worlds = plugin.Settings.getString("affectedWorlds").split(",");
    }

    public abstract boolean rollback(final CommandSender sender, final String admin, final String target, final int time);
}
