package com.mcbans.firestar.mcbans.rollback;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.BukkitInterface;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;

public class LbRollback extends BaseRollback{
    private LogBlock logblock;

    public LbRollback(BukkitInterface plugin) {
        super(plugin);

        Plugin lb = plugin.getServer().getPluginManager().getPlugin("LogBlock");
        if (lb != null){
            logblock = (LogBlock) lb;
        }
    }

    @Override
    public boolean rollback(CommandSender sender, String admin, String target, int time) {
        for (String world : worlds) {
            QueryParams params = new QueryParams(this.logblock);
            params.setPlayer(target);
            params.since = (time * plugin.Settings.getInteger("backDaysAgo")); // TODO: Check here. really correct?
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

        return true;
    }
}
