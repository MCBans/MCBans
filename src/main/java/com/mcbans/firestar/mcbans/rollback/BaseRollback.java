package com.mcbans.firestar.mcbans.rollback;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
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

        boolean allWorld = false;
        for (String world : worlds){
            if (world.trim().equalsIgnoreCase("*")){
                allWorld = true;
            }
        }
        if (allWorld){
            List<String> w = new ArrayList<String>(Bukkit.getWorlds().size());
            for (World world : Bukkit.getWorlds()){
                w.add(world.getName());
            }
            worlds = w.toArray(new String[0]);
        }
    }

    public abstract boolean rollback(final CommandSender sender, final String senderName, final String target);

    public boolean setPlugin(final Plugin plugin){
        return true;
    }
}
