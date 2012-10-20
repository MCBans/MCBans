package com.mcbans.firestar.mcbans.rollback;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.BukkitInterface;

public class CpRollback extends BaseRollback{
    private CoreProtect coreProtect;
    private CoreProtectAPI cpAPI;

    public CpRollback(BukkitInterface plugin) {
        super(plugin);

        Plugin cp = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
        if (cp != null){
            coreProtect = (CoreProtect) cp;
            cpAPI = coreProtect.getAPI();
        }
    }

    @Override
    public boolean rollback(CommandSender sender, String admin, String target, int time) {
        try {
            cpAPI.performRollback(
                    target,
                    86400 * plugin.Settings.getInteger("backDaysAgo"),
                    0, null, null, null);
            plugin.broadcastPlayer(admin, ChatColor.GREEN + "Rollback successful!");
        }catch (Exception e){
            plugin.broadcastPlayer(admin, ChatColor.RED + "Unable to rollback player!");
            if (plugin.Settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
            return false;
        }

        return true;
    }
}
