package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class ServerChoose implements Runnable {
    private final MCBans plugin;
    private final ActionLog log;

    public ServerChoose(MCBans plugin) {
        this.plugin = plugin;
        this.log = plugin.getLog();
    }

    @Override
    public void run() {
        plugin.notSelectedServer = true;
        log.info("Looking for fastest api server!");
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
                        log.info("API Server found: " + server + " :: response time: " + ft);
                    }
                }
            } catch (IllegalArgumentException e) {
            } catch (NullPointerException e) {
            }
        }
        log.info("Fastest server selected: " + plugin.apiServer + " :: response time: " + d);
        plugin.notSelectedServer = false;
    }
}