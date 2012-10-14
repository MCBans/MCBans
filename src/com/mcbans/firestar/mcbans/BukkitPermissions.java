package com.mcbans.firestar.mcbans;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class BukkitPermissions {
    private BukkitInterface MCBans;
    private ArrayList<String> banView = new ArrayList<String>();
    private ArrayList<String> joinView = new ArrayList<String>();
    private ArrayList<String> altsView = new ArrayList<String>();
    private ArrayList<String> kickView = new ArrayList<String>();

    public BukkitPermissions(Settings cf, BukkitInterface p) {
        MCBans = p;
    }

    public boolean isAllow(String PlayerName, String PermissionNode) {
        Player target = MCBans.getServer().getPlayer(PlayerName);
        return target != null && isAllow(target, PermissionNode);
    }

    public boolean isAllow(Player Player, String PermissionNode) {
        if (Player.hasPermission("mcbans." + PermissionNode)) {
            return true;
        }
        return false;
    }

    public void playerConnect(Player player) {
        if (player.hasPermission("mcbans.*")) {
            kickView.add(player.getName());
            banView.add(player.getName());
            joinView.add(player.getName());
            altsView.add(player.getName());
            return;
        }
        if (player.hasPermission("mcbans.kick.view")) {
            kickView.add(player.getName());
        }
        if (player.hasPermission("mcbans.ban.view")) {
            banView.add(player.getName());
        }
        if (player.hasPermission("mcbans.join.view")) {
            joinView.add(player.getName());
        }
        if (player.hasPermission("mcbans.alts.view")) {
            altsView.add(player.getName());
        }
    }

    public ArrayList<String> getPlayersKick() {
        return kickView;
    }

    public ArrayList<String> getPlayersBan() {
        return banView;
    }

    public ArrayList<String> getPlayersJoin() {
        return joinView;
    }

    public ArrayList<String> getPlayersAlts() {
        return altsView;
    }

    public void playerDisconnect(String playerName) {
        if (altsView.contains(playerName)) {
            altsView.remove(playerName);
        }
        if (joinView.contains(playerName)) {
            joinView.remove(playerName);
        }
        if (kickView.contains(playerName)) {
            kickView.remove(playerName);
        }
        if (banView.contains(playerName)) {
            banView.remove(playerName);
        }
    }
}