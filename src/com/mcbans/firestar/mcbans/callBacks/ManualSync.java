package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;

public class ManualSync implements Runnable {
    private final MCBans plugin;
    private final String commandSend;
    
    public ManualSync(final MCBans plugin, final String sender){
        this.plugin = plugin;
        this.commandSend = sender;
    }
    
    @Override
    public void run() {
        while(plugin.apiServer == null){
            //waiting for server select
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        
        if(plugin.syncRunning){
            Util.message(commandSend, ChatColor.GREEN + " Sync already in progress!" );
            return;
        }
        plugin.syncRunning = true;
        
        int fre = 0;
        try{
            boolean goNext = true;
            while(goNext){
                JsonHandler webHandle = new JsonHandler( plugin );
                HashMap<String, String> url_items = new HashMap<String, String>();
                url_items.put( "latestSync", String.valueOf(plugin.lastID) );
                url_items.put( "exec", "banSync" );
                JSONObject response = webHandle.hdl_jobj(url_items);
                try {
                    if(response.has("banned")){
                        fre += response.getJSONArray("banned").length();
                        if (response.getJSONArray("banned").length() > 0) {
                            for (int v = 0; v < response.getJSONArray("banned").length(); v++) {
                                String[] plyer = response.getJSONArray("banned").getString(v).split(";");
                                OfflinePlayer d = plugin.getServer().getOfflinePlayer(plyer[0]);
                                if (d != null){
                                    if(d.isBanned()){
                                        if(plyer[1].equals("u")){
                                            d.setBanned(false);
                                        }
                                    }else{
                                        if(plyer[1].equals("b")){
                                            d.setBanned(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(response.has("lastid")){
                        long h = response.getLong("lastid");
                        if(h != 0){
                            plugin.lastID = h;
                        }
                    }
                    goNext = response.has("more");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {}
            }
        } finally {
            plugin.syncRunning = false;
        }
        Util.message(commandSend, ChatColor.GREEN + " Sync finished, " + fre + " actions!" );
    }
}