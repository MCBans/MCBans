package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class Ping implements Runnable {
    private final BukkitInterface MCBans;
    private String commandSend = "";

    public Ping(BukkitInterface p, String player) {
        MCBans = p;
        commandSend = player;
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
        long pingTime = (System.currentTimeMillis());
        JsonHandler webHandle = new JsonHandler(MCBans);
        HashMap<String, String> items = new HashMap<String, String>();
        items.put("exec", "check");
        String urlReq = webHandle.urlparse(items);
        String jsonText = webHandle.request_from_api(urlReq);
        if (jsonText.equals("up")) {
            MCBans.broadcastPlayer(commandSend, ChatColor.GREEN + "API Server response time " + ((System.currentTimeMillis()) - pingTime)
                    + " milliseconds!");
        } else {
            MCBans.broadcastPlayer(commandSend, ChatColor.RED + "API appears to be down!");
        }

    }

}