package com.mcbans.firestar.mcbans.rollback;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public class CpRollback extends BaseRollback{
    public CpRollback(MCBans plugin) {
        super(plugin);
    }

    private CoreProtectAPI cpAPI;

    @Override
    public boolean rollback(CommandSender sender, String senderName, String target) {
        if (cpAPI == null) return false;

        try {
            // TODO: check async? maybe not
            cpAPI.performRollback(
                    target,
                    86400 * plugin.settings.getInteger("backDaysAgo"),
                    0, null, null, null);
        }catch (Exception e){
            Util.message(senderName, ChatColor.RED + "Unable to rollback player!");
            if (plugin.settings.getBoolean("isDebug")) {
                e.printStackTrace();
            }
            return false;
        }

        Util.message(senderName, ChatColor.GREEN + "Rollback successful!");
        return true;
    }

    @Override
    public boolean setPlugin(Plugin plugin){
        if (plugin == null) return false;

        if (plugin instanceof CoreProtect){
            this.cpAPI = ((CoreProtect) plugin).getAPI();
            return (cpAPI != null);
        }

        return false;
    }
}
