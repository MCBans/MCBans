package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class ServerChoose implements Runnable {
    private final MCBans plugin;

    public ServerChoose(MCBans plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.notSelectedServer = true;
        plugin.log("Looking for fastest api server!");
        long d = 99999;
        for (String server : plugin.apiServers) {
            try {
                long pingTime = (System.currentTimeMillis());
                JsonHandler webHandle = new JsonHandler(plugin);
                HashMap<String, String> items = new HashMap<String, String>();
                items.put("exec", "check");
                String urlReq = webHandle.urlparse(items);
                String jsonText = webHandle.request_from_api(urlReq, server);
                if (jsonText.equals("up")) {
                    long ft = ((System.currentTimeMillis()) - pingTime);
                    if (d > ft) {
                        d = ft;
                        plugin.apiServer = server;
                        plugin.log("API Server found: " + server + " :: response time: " + ft);
                    }
                }
            } catch (IllegalArgumentException e) {
            } catch (NullPointerException e) {
            }
        }
        plugin.log("Fastest server selected: " + plugin.apiServer + " :: response time: " + d);
        plugin.notSelectedServer = false;
    }
}