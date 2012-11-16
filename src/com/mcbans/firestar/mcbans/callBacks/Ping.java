package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;

public class Ping implements Runnable {
    private final MCBans plugin;
    private String commandSend = "";

    public Ping(MCBans plugin, String sender) {
        this.plugin = plugin;
        this.commandSend = sender;
    }

    @Override
    public void run() {
        while (plugin.notSelectedServer) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        long pingTime = (System.currentTimeMillis());
        JsonHandler webHandle = new JsonHandler(plugin);
        HashMap<String, String> items = new HashMap<String, String>();
        items.put("exec", "check");
        String urlReq = webHandle.urlparse(items);
        String jsonText = webHandle.request_from_api(urlReq);
        if (jsonText.equals("up")) {
            Util.message(commandSend, ChatColor.GREEN + "API Server response time " + ((System.currentTimeMillis()) - pingTime)
                    + " milliseconds!");
        } else {
            Util.message(commandSend, ChatColor.RED + "API appears to be down!");
        }
    }
}