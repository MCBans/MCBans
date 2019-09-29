package com.mcbans.plugin.request;

import java.util.HashMap;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.BaseCallback;
import com.mcbans.plugin.org.json.JSONObject;

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
            callback.error("&cCould not select or detect the MCBans API server.");
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
