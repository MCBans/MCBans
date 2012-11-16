package com.mcbans.firestar.mcbans.util;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;

public class Util {
    /****************************************/
    // Messaging
    /****************************************/
    /**
     * Send message to player
     * @param target target Player
     * @param msg send message, non prefix
     */
    public static void message(final Player target, String msg) {
        if (target != null && msg != null){
            target.sendMessage(MCBans.getPrefix() + " " + msg);
        }
    }

    /**
     * Send message to player
     * @param target target CommandSender
     * @param msg send message, non prefix
     */
    public static void message(final CommandSender target, String msg) {
        if (target != null && msg != null){
            target.sendMessage(MCBans.getPrefix() + " " + msg);
        }
    }

    /**
     * Send message to player
     * @param playerName target player name
     * @param msg send message, non prefix
     */
    public static void message(final String playerName, String msg) {
        final Player target = Bukkit.getServer().getPlayer(playerName);
        if (target != null) {
            target.sendMessage(MCBans.getPrefix() + " " + msg);
        } else {
            ActionLog.getInstance().info(msg);
        }
    }

    /**
     * Broadcast online players
     * @param msg send message, non prefix
     */
    public static void broadcastMessage(String msg) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendMessage(MCBans.getPrefix() + " " + msg);
        }
    }

    /****************************************/
    // Etc utils
    /****************************************/
    /**
     * Same function of PHP join(array, delimiter)
     * @param s Collection
     * @param delimiter Delimiter character
     * @return Joined string
     */
    public static String join(Collection<?> s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<?> iter = s.iterator();

        // Loop elements
        while (iter.hasNext()){
            buffer.append(iter.next());
            // if has next element, put delimiter
            if (iter.hasNext()){
                buffer.append(delimiter);
            }
        }
        // return buffer string
        return buffer.toString();
    }
}
