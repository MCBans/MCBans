package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.permission.Perms;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;

public class Lookup implements Runnable {
    private final MCBans plugin;
    private final ActionLog log;

    private final String playerName;
    private final String senderName;

    public Lookup(MCBans plugin, String playerName, String senderName) {
        this.plugin = plugin;
        this.log = plugin.getLog();

        this.playerName = playerName;
        this.senderName = senderName;
    }

    @Override
    public void run() {
        while (plugin.apiServer == null) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        log.info(senderName + " has looked up the " + playerName + "!");
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
                    log.severe("The server API key has been disabled by an MCBans Administrator");
                    log.severe("To appeal this decision, please contact an administrator");
                }
            } else {
                Util.message(senderName, ChatColor.RED + "There was an error while parsing the data! [JSON Error]");
                log.severe("JSON error while trying to parse lookup data!");
            }
        } catch (NullPointerException e) {
            Util.message(senderName, ChatColor.RED + "There was an error while polling the API!");
            log.severe("Unable to reach MCBans Master server!");
        }
    }
}