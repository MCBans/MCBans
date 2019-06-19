package com.mcbans.firestar.mcbans.util;

import com.google.common.base.Objects;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultStuff {

    static {
        if(hasVault()) {
            chat = Bukkit.getServicesManager().getRegistration(Chat.class);
            perms = Bukkit.getServicesManager().getRegistration(Permission.class);
        } else {
            chat = null;
            perms = null;
        }
    }

    private VaultStuff() {}

    private static final Object chat, perms;

    public static Object getChat() {
        return chat;
    }

    public static Object getPerms() {
        return perms;
    }

    public static boolean hasVault() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    public static void setDefault(OfflinePlayer p) {
        if(hasVault()) {
            Permission p2 = ((RegisteredServiceProvider<Permission>) perms).getProvider();

            for(String s : p2.getPlayerGroups("", p)) {
                p2.playerRemoveGroup("", p, s);
            }
        }
    }
}
