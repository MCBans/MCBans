package com.mcbans.firestar.mcbans.pluginInterface;

import java.util.HashMap;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class Disconnect implements Runnable {
    private BukkitInterface plugin;
    private String playerName;

    public Disconnect(BukkitInterface plugin, String playerName) {
        this.plugin = plugin;
        this.playerName = playerName;
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
        plugin.log(playerName + " has disconnected!");
        JsonHandler webhandle = new JsonHandler(plugin);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", playerName);
        url_items.put("exec", "playerDisconnect");
        webhandle.mainRequest(url_items);
    }
}