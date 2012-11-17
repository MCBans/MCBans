package com.mcbans.firestar.mcbans.callBacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class ServerChoose implements Runnable {
    private final MCBans plugin;
    private final ActionLog log;

    /* API Servers List */
    @SuppressWarnings("serial")
    private final List<String> apiServers = new ArrayList<String>(4) {{
        add("api01.cluster.mcbans.com");
        add("api02.cluster.mcbans.com");
        add("api03.cluster.mcbans.com");
        add("api.mcbans.com");
    }};

    public ServerChoose(MCBans plugin) {
        this.plugin = plugin;
        this.log = plugin.getLog();
    }

    @Override
    public void run() {
        plugin.apiServer = null;
        log.info("Looking for fastest api server!");

        long d = 99999;
        String fastest = null;
        for (String server : apiServers) {
            try {
                long pingTime = (System.currentTimeMillis());
                JsonHandler webHandle = new JsonHandler(plugin);
                HashMap<String, String> items = new HashMap<String, String>();
                items.put("exec", "check");
                String urlReq = webHandle.urlparse(items);
                String jsonText = webHandle.request_from_api(urlReq, server);
                if (jsonText.equals("up")) {
                    long ft = ((System.currentTimeMillis()) - pingTime);
                    log.info("API Server found: " + server + " :: response time: " + ft);

                    if (d > ft) {
                        d = ft;
                        fastest = server;
                    }
                }
            } catch (IllegalArgumentException e) {
            } catch (NullPointerException e) {
            }
        }

        if (fastest != null){
            log.info("Fastest server selected: " + fastest + " :: response time: " + d);
        }else{
            log.warning("Can't reach any MCBans API Servers!");
            log.warning("Check your network or please wait until the recovery!");
        }
        plugin.apiServer = fastest;
    }
}