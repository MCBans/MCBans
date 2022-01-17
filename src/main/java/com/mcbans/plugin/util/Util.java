package com.mcbans.plugin.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.permission.Perms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class Util {
  private static final String IP_PATTERN = "^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$";

  /****************************************/
  // Messaging
  /****************************************/
  /**
   * Send message to player
   *
   * @param target target CommandSender
   * @param msg    send message, non prefix
   */
  public static void message(final CommandSender target, String msg) {
    if (msg != null) {
      msg = MCBans.getPrefix() + ChatColor.WHITE +" "+ msg;
      String finalMsg = msg;
      if (target != null && target instanceof Player) {
        new BukkitRunnable() {
          @Override
          public void run() {
            target.sendMessage(finalMsg);
          }
        }.runTaskLater(MCBans.getPlugin(MCBans.class), 10);
      } else {
        new BukkitRunnable() {
          @Override
          public void run() {
            // use craftbukkit for sending coloured message to console. Refer class #ColouredConsoleSender
            //org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance().sendMessage();
            Bukkit.getServer().getConsoleSender().sendMessage(finalMsg);
            //ActionLog.getInstance().info(msg);
          }
        }.runTaskLater(MCBans.getPlugin(MCBans.class), 10);

      }
    }
  }
  public static void messages(final CommandSender target, String[] messages) {
    if (messages != null) {
      String[] finalMsg = messages;
      if (target != null && target instanceof Player) {
        new BukkitRunnable() {
          @Override
          public void run() {
            for (int i = 0; i < finalMsg.length; i++) {
              target.sendMessage(MCBans.getPrefix() + ChatColor.WHITE +" "+ finalMsg[i]);
            }
          }
        }.runTaskLater(MCBans.getPlugin(MCBans.class), 10);
      } else {
        new BukkitRunnable() {
          @Override
          public void run() {
            // use craftbukkit for sending coloured message to console. Refer class #ColouredConsoleSender
            //org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance().sendMessage();
            for (int i = 0; i < finalMsg.length; i++) {
              Bukkit.getServer().getConsoleSender().sendMessage(MCBans.getPrefix() + ChatColor.WHITE +" "+ finalMsg[i]);
            }
            //ActionLog.getInstance().info(msg);
          }
        }.runTaskLater(MCBans.getPlugin(MCBans.class), 10);

      }
    }
  }

  /**
   * Send message to player
   *
   * @param playerName target player name
   * @param msg        send message, non prefix
   */
  public static void message(final String playerName, String msg) {
    final Player target = Bukkit.getServer().getPlayer(playerName);
    message(target, msg);
  }

  public static void messages(final String playerName, String[] messages) {
    final Player target = Bukkit.getServer().getPlayer(playerName);
    messages(target, messages);
  }

  /**
   * Broadcast online players
   *
   * @param msg send message, non prefix
   */
  public static void broadcastMessage(String msg) {
    new BukkitRunnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
          if (Perms.ANNOUNCE.has(player) || MCBans.AnnounceAll) {
            player.sendMessage(MCBans.getPrefix() + ChatColor.WHITE +" "+  msg);
          }
        }
      }
    }.runTaskLater(MCBans.getPlugin(MCBans.class), 10);
  }

  public static void broadcastMessages(String[] messages) {
    new BukkitRunnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
          if (Perms.ANNOUNCE.has(player) || MCBans.AnnounceAll) {
            for (int i = 0; i < messages.length; i++) {
              player.sendMessage(MCBans.getPrefix() + ChatColor.WHITE +" "+  messages[i]);
            }
          }
        }
      }
    }.runTaskLater(MCBans.getPlugin(MCBans.class), 10);
  }

  /**
   * Coloring message
   *
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
   *
   * @param s         Collection
   * @param delimiter Delimiter character
   * @return Joined string
   */
  public static String join(Collection<?> s, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    Iterator<?> iter = s.iterator();

    // Loop elements
    while (iter.hasNext()) {
      buffer.append(iter.next());
      // if has next element, put delimiter
      if (iter.hasNext()) {
        buffer.append(delimiter);
      }
    }
    // return buffer string
    return buffer.toString();
  }

  /**
   * Check player name valid or not
   *
   * @param name player name to check
   * @return true if valid name
   */
  public static boolean isValidName(final String name) {
    if (name == null) return false;

    final String regex = "^[A-Za-z0-9_]{3,16}$";
    if (!name.matches(regex)) {
      return false;
    }

    return true;
  }

  public static boolean isValidUUID(final String name) {
    if (name == null) return false;

    final String regex = "^[A-Za-z0-9]{32}$";
    if (!name.replaceAll("(?im)-", "").matches(regex)) {
      return false;
    }

    return true;
  }

  /**
   * Check string is integer
   *
   * @param str String text to check
   * @return true if cast to int successfully
   */
  public static boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   * Check string is double
   *
   * @param str String text to check
   * @return true if cast to double successfully
   */
  public static boolean isDouble(String str) {
    try {
      Double.parseDouble(str);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public static boolean checkVault(Player sender, OfflinePlayer victim) {
    if (!VaultStuff.hasVault()) {
      return true;
    }

    try {
      Chat c = ((RegisteredServiceProvider<Chat>) VaultStuff.getChat()).getProvider();

      Permission p = ((RegisteredServiceProvider<Permission>) VaultStuff.getPerms()).getProvider();

      //Priority of sender
      int gpri = c.getGroupInfoInteger("", p.getPrimaryGroup(sender), "mcbansPriority", 0);

      //Priority of victim
      int gpri2 = c.getGroupInfoInteger("", p.getPrimaryGroup("", victim), "mcbansPriority", 0);

      if (gpri > gpri2) {
        return true;
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return true;
    } catch (ClassCastException e) {
      e.printStackTrace();
      return true;
    }
    return false;
  }
}
