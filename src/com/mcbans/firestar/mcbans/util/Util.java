package com.mcbans.firestar.mcbans.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.permission.Perms;

public class Util {
    private static final String IP_PATTERN = 
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    
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
                //org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance().sendMessage();
                Bukkit.getServer().getConsoleSender().sendMessage(msg);
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
        	if(Perms.ANNOUNCE.has(player) || MCBans.AnnounceAll){
        		player.sendMessage(MCBans.getPrefix() + " " + msg);
        	}
        }
    }

    /**
     * Coloring message
     * @param msg non-colored message
     */
    public static String color(String msg) {
        return msg.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "\u00A7$1");
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

    public static boolean isValidUUID(final String name){
        if (name == null) return false;

        final String regex = "^[A-Za-z0-9_]{32}$";
        if (!Pattern.compile(regex).matcher(name.replaceAll("(?im)-", "")).matches()){
            return false;
        }

        return true;
    }
    
    /**
     * Check string is integer
     * @param str String text to check
     * @return true if cast to int successfully
     */
    public static boolean isInteger(String str) {
        try{
            Integer.parseInt(str);
        }catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    /**
     * Check string is double
     * @param str String text to check
     * @return true if cast to double successfully
     */
    public static boolean isDouble(String str) {
        try{
            Double.parseDouble(str);
        }catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    /**
     * Check string is valid IP
     * @param str String to check
     * @return tru if valid ip address
     */
    public static boolean isValidIP(String str){
        if (str == null) return false;
        Matcher matcher = Pattern.compile(IP_PATTERN).matcher(str);
        return matcher.matches();
    }
}
