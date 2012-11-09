package com.mcbans.firestar.mcbans.callBacks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
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
    private String commandSend = "";
    public ManualResync(MCBans plugin, String sender){
        this.plugin = plugin;
        this.commandSend = sender;
    }
    @Override
    public void run() {
        if(plugin.syncRunning==true){
            Util.message(commandSend, ChatColor.GREEN + " Sync already in progress!" );
            return;
        }
        plugin.syncRunning = true;
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
                if(plugin.lastID == 0){
                    if(response.has("timerecieved")){
                        plugin.timeRecieved = response.getLong("timerecieved");
                    }
                }
                if(response.has("lastid")){
                    plugin.lastID = response.getLong("lastid");
                }
                if(response.has("more")){
                    goNext = true;
                }else{
                    goNext = false;
                }
            } catch (JSONException e) {
                if(plugin.settings.getBoolean("isDebug")){
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                if(plugin.settings.getBoolean("isDebug")){
                    e.printStackTrace();
                }
            }
            if(plugin.lastID == startID){
                f++;
            }else{
                f=1;
            }
            if(f>5){
                goNext = false;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        plugin.syncRunning = false;
        Util.message(commandSend, ChatColor.GREEN + " Sync finished" );
        this.save();
    }
    public void save(){
        try {
            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("plugins/mcbans/sync.last"), "UTF-8");
            BufferedWriter fout = new BufferedWriter(writer);
            fout.write(String.valueOf(plugin.lastID));
            fout.close();
            writer.close();
        } catch (Exception e) {
            if(plugin.settings.getBoolean("isDebug")){
                e.printStackTrace();
            }
        }
    }
}