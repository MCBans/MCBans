package com.mcbans.firestar.mcbans.pluginInterface;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.request.JsonHandler;

import java.util.HashMap;

public class Disconnect implements Runnable {
    private BukkitInterface MCBans;
    private String PlayerName;

    public Disconnect(BukkitInterface p, String Player) {
        MCBans = p;
        PlayerName = Player;
    }

    @Override
    public void run() {
        while (MCBans.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        MCBans.log(PlayerName + " has disconnected!");
        JsonHandler webhandle = new JsonHandler(MCBans);
        HashMap<String, String> url_items = new HashMap<String, String>();
        url_items.put("player", PlayerName);
        url_items.put("exec", "playerDisconnect");
        webhandle.mainRequest(url_items);
    }
}