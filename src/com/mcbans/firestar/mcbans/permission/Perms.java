package com.mcbans.firestar.mcbans.permission;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.util.Util;

public enum Perms {
    /* Permission Nodes */

    // Admin permission
    ADMIN           ("admin"),
    
    // Ban permissions
    BAN_GLOBAL      ("ban.global"),
    BAN_LOCAL       ("ban.local"),
    BAN_TEMP        ("ban.temp"),
    BAN_ROLLBACK    ("ban.rollback"),
    BAN_IP          ("ban.ip"),
    UNBAN           ("unban"),
    KICK            ("kick"),

    // View permissions
    VIEW_ALTS       ("view.alts"),
    VIEW_BANS       ("view.bans"),
    VIEW_STAFF      ("view.staff"),
    VIEW_PREVIOUS   ("view.previous"),
    VIEW_PROXY      ("view.proxy"),
    ANNOUNCE        ("announce"),
    HIDE_VIEW       ("hideview"),
    
    // Exempt
    EXEMPT_KICK     ("kick.exempt"),
    EXEMPT_BAN      ("ban.exempt"),
    EXEMPT_MAXALTS	("maxalts.exempt"),

    // Others
    LOOKUP_PLAYER   ("lookup.player"),
    LOOKUP_BAN      ("lookup.ban"),
    LOOKUP_ALT      ("lookup.alt"),
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
        return has(MCBans.getInstance().getServer().getPlayer(playerName));
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
                Util.message(player, message);
            }
        }
    }
    
    public String getNode(){
        return this.node;
    }

    public Set<Player> getPlayers(){
        Set<Player> players = new HashSet<Player>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            if (this.has(player)){
                players.add(player);
            }
        }
        return players;
    }
    
    public Set<String> getPlayerNames(){
        Set<String> names = new HashSet<String>();
        for (Player player : getPlayers()){
            names.add(player.getName());
        }
        return names;
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