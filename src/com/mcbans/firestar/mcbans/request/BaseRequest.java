package com.mcbans.firestar.mcbans.request;

import java.util.HashMap;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.callBacks.BaseCallback;
import com.mcbans.firestar.mcbans.org.json.JSONObject;

public abstract class BaseRequest<Callback extends BaseCallback> implements Runnable{
    protected final MCBans plugin;
    protected final ActionLog log;

    protected HashMap<String, String> items;
    protected Callback callback;

    public BaseRequest(final MCBans plugin, final Callback callback){
        this.plugin = plugin;
        this.log = plugin.getLog();
        this.callback = callback;

        this.items = new HashMap<String, String>();
    }

    @Override
    public void run(){
        if (!checkServer()){
            callback.error("&cCould not select or detect MCBans API Server!");
            return;
        }
        execute();
    }

    protected abstract void execute();

    private boolean checkServer(){
        while (plugin.apiServer == null) {
            // waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }

        return (plugin.apiServer != null);
    }

    protected void request(){
        JsonHandler webHandle = new JsonHandler(plugin);
        webHandle.mainRequest(items);
    }

    protected String request_String(){
        JsonHandler webHandle = new JsonHandler(plugin);
        String urlReq = webHandle.urlparse(items);
        return webHandle.request_from_api(urlReq);
    }

    protected JSONObject request_JOBJ(){
        JsonHandler webHandle = new JsonHandler(plugin);
        return webHandle.hdl_jobj(items);
    }
}
