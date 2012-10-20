package com.mcbans.firestar.mcbans.callBacks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class BanSync implements Runnable {
    private final BukkitInterface MCBans;

    public BanSync(BukkitInterface p){
        MCBans = p;
        this.load();
    }
    @Override
    public void run(){		
        while(true){
            int syncInterval = ((60*1000)*MCBans.Settings.getInteger("syncInterval"));
            if(syncInterval<((60*1000)*5)){
                syncInterval=((60*1000)*5);
            }
            while(MCBans.notSelectedServer){
                //waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            this.mainRequest();
            MCBans.lastSync = System.currentTimeMillis()/1000;
            try {
                Thread.sleep(syncInterval);
            } catch (InterruptedException e) {
            }
        }
    }
    public void goRequest() {
        this.mainRequest();
    }
    private void mainRequest(){
        if(MCBans.lastID==0){
            this.initialSync();
            this.save();
        }else{
            this.startSync();
            this.save();
        }
    }
    public void initialSync(){
        if(MCBans.syncRunning==true){
            return;
        }
        MCBans.syncRunning = true;
        boolean goNext = true;
        int f = 1;
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
    }
    public void startSync(){
        if(MCBans.syncRunning==true){
            return;
        }
        MCBans.syncRunning = true;
        boolean goNext = true;
        int f =1;
        while(goNext){
            long startID = MCBans.lastID;
            JsonHandler webHandle = new JsonHandler( MCBans );
            HashMap<String, String> url_items = new HashMap<String, String>();
            url_items.put( "latestSync", String.valueOf(MCBans.lastID) );
            url_items.put( "exec", "banSyncNew" );
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
                if(response.has("lastid")){
                    long h = response.getLong("lastid");
                    if(h != 0){
                        MCBans.lastID = h;
                    }
                }
                if(response.has("more")){
                    goNext = true;
                }else{
                    goNext = false;
                }
            } catch (NullPointerException e) {
                if(MCBans.Settings.getBoolean("isDebug")){
                    e.printStackTrace();
                }
            } catch (JSONException e) {
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
    public void load(){
        File f = new File("plugins/mcbans/sync.last");
        if(f.exists()!=true){
            MCBans.lastID=0;
            return;
        }
        String strLine="";
        try {
            BufferedReader i = new BufferedReader(new InputStreamReader
                    (new FileInputStream("plugins/mcbans/sync.last"),"UTF8"));
            String line = null;
            while (( line = i.readLine()) != null){
                strLine += line;
            }
            i.close();
            MCBans.lastID=Integer.valueOf(strLine);
        } catch (Exception e) {
            if(MCBans.Settings.getBoolean("isDebug")){
                e.printStackTrace();
            }
        }
    }
}