package com.mcbans.firestar.mcbans.callBacks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class ManualResync implements Runnable {
    private final BukkitInterface MCBans;
    private String commandSend = "";
    public ManualResync(BukkitInterface p, String player){
        MCBans = p;
        commandSend = player;
    }
    @Override
    public void run() {
        if(MCBans.syncRunning==true){
            MCBans.broadcastPlayer(commandSend, ChatColor.GREEN + " Sync already in progress!" );
            return;
        }
        MCBans.syncRunning = true;
        boolean goNext = true;
        int f = 1;
        MCBans.last_req=0;
        MCBans.lastID = 0;
        MCBans.timeRecieved = 0;
        while(goNext){
            long startID = MCBans.lastID;
            JsonHandler webHandle = new JsonHandler( MCBans );
            HashMap<String, String> url_items = new HashMap<String, String>();
            url_items.put( "latestSync", String.valueOf(MCBans.lastID) );
            url_items.put( "timeRecieved", String.valueOf(MCBans.timeRecieved) );
            url_items.put( "exec", "banSyncInitialNew" );
            JSONObject response = webHandle.hdl_jobj(url_items);
            try {
                if(response.has("banned")){
                    if (response.getJSONArray("banned").length() > 0) {
                        for (int v = 0; v < response.getJSONArray("banned").length(); v++) {
                            String[] plyer = response.getJSONArray("banned").getString(v).split(";");
                            OfflinePlayer d = MCBans.getServer().getOfflinePlayer(plyer[0]);
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
                if(MCBans.lastID == 0){
                    if(response.has("timerecieved")){
                        MCBans.timeRecieved = response.getLong("timerecieved");
                    }
                }
                if(response.has("lastid")){
                    MCBans.lastID = response.getLong("lastid");
                }
                if(response.has("more")){
                    goNext = true;
                }else{
                    goNext = false;
                }
            } catch (JSONException e) {
                if(MCBans.Settings.getBoolean("isDebug")){
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                if(MCBans.Settings.getBoolean("isDebug")){
                    e.printStackTrace();
                }
            }
            if(MCBans.lastID == startID){
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
        MCBans.syncRunning = false;
        MCBans.broadcastPlayer(commandSend, ChatColor.GREEN + " Sync finished" );
        this.save();
    }
    public void save(){
        try {
            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("plugins/mcbans/sync.last"), "UTF-8");
            BufferedWriter fout = new BufferedWriter(writer);
            fout.write(String.valueOf(MCBans.lastID));
            fout.close();
            writer.close();
        } catch (Exception e) {
            if(MCBans.Settings.getBoolean("isDebug")){
                e.printStackTrace();
            }
        }
    }
}