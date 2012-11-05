package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;

public class Lookup implements Runnable {
    private BukkitInterface plugin;
    private String playerName;
    private String senderName;

    public Lookup(BukkitInterface plugin, String playerName, String senderName) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.senderName = senderName;
    }

    @Override
    public void run() {
        while (plugin.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        plugin.log(senderName + " has looked up the " + playerName + "!");
        HashMap<String, String> url_items = new HashMap<String, String>();
        JsonHandler webHandle = new JsonHandler(plugin);
        url_items.put("player", playerName);
        url_items.put("admin", senderName);
        url_items.put("exec", "playerLookup");
        JSONObject result = webHandle.hdl_jobj(url_items);
        try {
            Util.message(senderName, "Player " + ChatColor.DARK_AQUA + playerName + ChatColor.WHITE + " has " + ChatColor.DARK_RED
                    + result.getString("total") + " ban(s)" + ChatColor.WHITE + " and " + ChatColor.BLUE + result.getString("reputation") + " REP"
                    + ChatColor.WHITE + ".");
            if (result.getJSONArray("global").length() > 0) {
                Util.message(senderName, ChatColor.DARK_RED + "Global bans");
                for (int v = 0; v < result.getJSONArray("global").length(); v++) {
                    Util.message(senderName, result.getJSONArray("global").getString(v));
                }
            }
            if (result.getJSONArray("local").length() > 0) {
                Util.message(senderName, ChatColor.GOLD + "Local bans");
                for (int v = 0; v < result.getJSONArray("local").length(); v++) {
                    Util.message(senderName, result.getJSONArray("local").getString(v));
                }
            }
            if (result.getJSONArray("other").length() > 0) {
                for (int v = 0; v < result.getJSONArray("other").length(); v++) {
                    Util.message(senderName, result.getJSONArray("other").getString(v));
                }
            }
        } catch (JSONException e) {
            if (result.toString().contains("error")) {
                if (result.toString().contains("Server Disabled")) {
                    Perms.VIEW_BANS.message(ChatColor.RED + "Server Disabled by an MCBans Admin");
                    Perms.VIEW_BANS.message("MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
                    plugin.log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
                    plugin.log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
                }
            } else {
                Util.message(senderName, ChatColor.RED + "There was an error while parsing the data! [JSON Error]");
                plugin.log(LogLevels.SEVERE, "JSON error while trying to parse lookup data!");
            }
        } catch (NullPointerException e) {
            Util.message(senderName, ChatColor.RED + "There was an error while polling the API!");
            plugin.log(LogLevels.SEVERE, "Unable to reach MCBans Master server!");
        }
    }
}