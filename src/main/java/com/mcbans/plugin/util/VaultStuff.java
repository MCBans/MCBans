package com.mcbans.plugin.util;

import com.google.common.base.Objects;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class VaultStuff {

    static {
        if(hasVault()) {
            perms = Bukkit.getServicesManager().getRegistration(Permission.class);
        } else {
            perms = null;
        }
    }

    private VaultStuff() {}

    private static final Object perms;

    public static Object getPerms() {
        return perms;
    }

    /*public static boolean priorityCheck(Player sender, OfflinePlayer target){

    }
    public static boolean priorityCheck(Player sender, OfflinePlayer target){

    }*/

    public static boolean hasVault() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }
}
