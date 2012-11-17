package com.mcbans.firestar.mcbans.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.MCBans;

public class Util {
    /****************************************/
    // Messaging
    /****************************************/
    /**
     * Send message to player
     * @param target target CommandSender
     * @param msg send message, non prefix
     */
    public static void message(final CommandSender target, String msg) {
        if (msg != null){
            msg = MCBans.getPrefix() + " " + ChatColor.WHITE + msg;
            if (target != null && target instanceof Player){
                target.sendMessage(msg);
            }else{
                // use craftbukkit for sending coloured message to console. Refer class #ColouredConsoleSender
                org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance().sendMessage(msg);
                //ActionLog.getInstance().info(msg);
            }
        }
    }

    /**
     * Send message to player
     * @param playerName target player name
     * @param msg send message, non prefix
     */
    public static void message(final String playerName, String msg) {
        final Player target = Bukkit.getServer().getPlayer(playerName);
        message(target, msg);
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

    /**
     * Check player name valid or not
     * @param name player name to check
     * @return true if valid name
     */
    public static boolean isValidName(final String name){
        if (name == null) return false;

        final String regex = "^[A-Za-z0-9_]{2,16}$";
        if (!Pattern.compile(regex).matcher(name).matches()){
            return false;
        }

        return true;
    }
}
