package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class Ping implements Runnable {
    private final BukkitInterface plugin;
    private String commandSend = "";

    public Ping(BukkitInterface plugin, String sender) {
        this.plugin = plugin;
        this.commandSend = sender;
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
        long pingTime = (System.currentTimeMillis());
        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> items = new HashMap<String, String>();
        items.put("exec", "check");
        String urlReq = webHandle.urlparse(items);
        String jsonText = webHandle.request_from_api(urlReq);
        if (jsonText.equals("up")) {
            plugin.broadcastPlayer(commandSend, ChatColor.GREEN + "API Server response time " + ((System.currentTimeMillis()) - pingTime)
                    + " milliseconds!");
        } else {
            plugin.broadcastPlayer(commandSend, ChatColor.RED + "API appears to be down!");
        }

    }

}