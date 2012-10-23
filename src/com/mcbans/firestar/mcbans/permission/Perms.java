package com.mcbans.firestar.mcbans.permission;

import org.bukkit.permissions.Permissible;

import com.mcbans.firestar.mcbans.BukkitInterface;

public enum Perms {
    /* Nodes */
    // Special permissions
    //ADMIN           ("admin"), // disuse

    // Ban permissions
    BAN_LOCAL       ("ban.local"),
    BAN_GLOBAL      ("ban.global"),
    BAN_TEMP        ("ban.temp"),
    BAN_ROLLBACK    ("ban.rollback"),

    // Unban permission
    UNBAN           ("unban"),

    // View permissions
    //VIEW_KICK       ("view.kick"), // disuse
    VIEW_JOIN       ("view.join"),
    VIEW_ALTS       ("view.alts"),

    // Hide permissions
    HIDE_ALTS       ("hide.alts"),

    // Others
    KICK            ("kick"),
    LOOKUP          ("lookup"),

    ;

    // Node header
    final String HEADER = "mcbans.";
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

    /* PermissionHandler */
    private static PermissionHandler handler = null;
    public static void setupPermissionHandler(){
        if (handler == null){
            handler = PermissionHandler.getInstance();
        }
        handler.setupPermissions();
    }
}