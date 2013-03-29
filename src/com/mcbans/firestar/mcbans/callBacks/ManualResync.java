package com.mcbans.firestar.mcbans.callBacks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import com.mcbans.firestar.mcbans.util.Util;

public class ManualResync implements Runnable {
    private final MCBans plugin;
    private final String commandSend;
    
    public ManualResync(final MCBans plugin, final String sender){
        this.plugin = plugin;
        this.commandSend = sender;
    }
    
    @Override
    public void run() {
        if(plugin.syncRunning){
            Util.message(commandSend, ChatColor.GREEN + " Sync already in progress!" );
            return;
        }
        plugin.syncRunning = true;
        
        try{
            boolean goNext = true;
            int f = 1;
            plugin.last_req=0;
            plugin.lastID = 0;
            plugin.timeRecieved = 0;
            
            while(goNext){
                long startID = plugin.lastID;
                JsonHandler webHandle = new JsonHandler( plugin );
                HashMap<String, String> url_items = new HashMap<String, String>();
                url_items.put( "latestSync", String.valueOf(plugin.lastID) );
                url_items.put( "timeRecieved", String.valueOf(plugin.timeRecieved) );
                url_items.put( "exec", "banSyncInitialNew" );
                JSONObject response = webHandle.hdl_jobj(url_items);
                try {
                    if(response.has("banned")){
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
                    if(plugin.lastID == 0){
                        if(response.has("timerecieved")){
                            plugin.timeRecieved = response.getLong("timerecieved");
                        }
                    }
                    if(response.has("lastid")){
                        plugin.lastID = response.getLong("lastid");
                    }
                    goNext = response.has("more");
                } catch (JSONException e) {
                    if(plugin.getConfigs().isDebug()){
                        e.printStackTrace();
                    }
                } catch (NullPointerException e) {
                    if(plugin.getConfigs().isDebug()){
                        e.printStackTrace();
                    }
                }
                if(plugin.lastID == startID){
                    f++;
                }else{
                    f = 1;
                }
                if(f > 5){
                    goNext = false;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {}
            }
        } finally {
            plugin.syncRunning = false;
        }
        
        Util.message(commandSend, ChatColor.GREEN + " Sync finished");
        this.save();
    }

    public void save(){
        Writer writer = null;
        BufferedWriter fout = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(new File(plugin.getDataFolder(), "sync.last")), "UTF-8");
            fout = new BufferedWriter(writer);
            fout.write(String.valueOf(plugin.lastID));
        } catch (Exception e) {
            if(plugin.getConfigs().isDebug()){
                e.printStackTrace();
            }
        } finally {
            if (fout != null){
                try { fout.close(); } 
                catch (IOException ignore) {}
            }
            if (writer != null){
                try { writer.close(); } 
                catch (IOException ignore) {}
            }
        }
    }
}
