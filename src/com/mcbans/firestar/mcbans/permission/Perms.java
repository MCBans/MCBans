package com.mcbans.firestar.mcbans.permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import com.mcbans.firestar.mcbans.BukkitInterface;

public enum Perms {
    /* Permission Nodes */

    // Admin permission
    ADMIN           ("admin"),

    // Ban permissions
    BAN_GLOBAL      ("ban.global"),
    BAN_LOCAL       ("ban.local"),
    BAN_TEMP        ("ban.temp"),
    BAN_ROLLBACK    ("ban.rollback"),

    // Unban permission
    UNBAN           ("unban"),

    // View permissions
    //VIEW_KICK       ("view.kick"), // disuse
    //VIEW_JOIN       ("view.join"), // disuse
    VIEW_ALTS       ("view.alts"),
    VIEW_BANS       ("view.bans"),

    // Hide permissions
    HIDE_ALTS       ("hide.alts"), // TODO:not used

    // Others
    KICK            ("kick"),
    LOOKUP          ("lookup"),

    ;

    // Node header
    final static String HEADER = "mcbans.";
    private String node;

    /**
     * Constructor
     * @param node Permission node excluded the header
     */
    Perms(final String node){
        this.node = HEADER + node;
    }


    /**
     * Check permissible has this permission.
     * @param perm check target Sender, Player etc.
     * @return true if permissible has this permission.
     */
    public boolean has(final Permissible perm){
        if (perm == null) return false;
        return handler.has(perm, this.node);
    }

    /**
     * Check player has this permission.
     * @param playerName check target player name.
     * @return true if permissible has this permission.
     */
    public boolean has(final String playerName){
        if (playerName == null) return false;
        return has(BukkitInterface.getInstance().getServer().getPlayer(playerName));
    }

    /**
     * Check permissible has that permission.
     * @param perm check target Sender, Player etc.
     * @param node Permission node excluded the header
     * @return true if permissible has that permission.
     */
    public static boolean has(final Permissible perm, String node) {
        if (perm == null || node == null) return false;
        return handler.has(perm, HEADER + node);
    }

    /**
     * Send message to players has this permission.
     * @param message send message.
     */
    public void message(final String message){
        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            if (this.has(player)){
                player.sendMessage(BukkitInterface.getInstance().Settings.getPrefix() + " " + message);
            }
        }
    }

    /* PermissionHandler */
    private static PermissionHandler handler = null;
    public static void setupPermissionHandler(){
        if (handler == null){
            handler = PermissionHandler.getInstance();
        }
        handler.setupPermissions();
    }
}