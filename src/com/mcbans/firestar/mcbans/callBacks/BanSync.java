package com.mcbans.firestar.mcbans.callBacks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;

public class BanSync implements Runnable {
    private final MCBans plugin;
    private final File syncFile;

    public BanSync(MCBans plugin){
        this.plugin = plugin;
        this.syncFile = new File(plugin.getDataFolder(), "sync.last");
        this.load();
    }

    @Override
    public void run(){
        while(true){
            int syncInterval = ((60 * 1000) * plugin.getConfigs().getSyncInterval());
            if(syncInterval < ((60 * 1000) * 5)){
                syncInterval = ((60 * 1000) * 5);
            }
            while(plugin.apiServer == null){
                //waiting for server select
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }

            // check isEnable auto syncing feature
            if (plugin.getConfigs().isEnableAutoSync()){
                this.mainRequest();
                plugin.lastSync = System.currentTimeMillis() / 1000;
            }

            try {
                Thread.sleep(syncInterval);
            } catch (InterruptedException e) {}
        }
    }

    public void goRequest() {
        this.mainRequest();
    }

    private void mainRequest(){
        if(plugin.lastID == 0){
            this.initialSync();
        }else{
            this.startSync();
        }
        this.save();
    }

    public void initialSync(){
        if(plugin.syncRunning){
            return;
        }
        plugin.syncRunning = true;
        
        try{
            boolean goNext = true;
            int f = 1;
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
                    if(response.has("more")){
                        goNext = true;
                    }else{
                        goNext = false;
                    }
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
    }

    public void startSync(){
        if(plugin.syncRunning){
            return;
        }
        plugin.syncRunning = true;
        
        try{
            boolean goNext = true;
            int f = 1;
            while(goNext){
                long startID = plugin.lastID;
                JsonHandler webHandle = new JsonHandler( plugin );
                HashMap<String, String> url_items = new HashMap<String, String>();
                url_items.put( "latestSync", String.valueOf(plugin.lastID) );
                url_items.put( "exec", "banSyncNew" );
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
                    if(response.has("lastid")){
                        long h = response.getLong("lastid");
                        if(h != 0){
                            plugin.lastID = h;
                        }
                    }
                    if(response.has("more")){
                        goNext = true;
                    }else{
                        goNext = false;
                    }
                } catch (NullPointerException e) {
                    if(plugin.getConfigs().isDebug()){
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
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
    }
    public void save(){
        Writer writer = null;
        BufferedWriter fout = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(syncFile), "UTF-8");
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
    public void load(){
        if(syncFile.exists() != true){
            plugin.lastID = 0;
            return;
        }
        String strLine = "";
        BufferedReader i = null;
        try {
            i = new BufferedReader(new InputStreamReader(new FileInputStream(syncFile), "UTF-8"));
            String line = null;
            while ((line = i.readLine()) != null){
                strLine += line;
            }
            plugin.lastID = Integer.valueOf(strLine);
        } catch (Exception e) {
            if(plugin.getConfigs().isDebug()){
                e.printStackTrace();
            }
        } finally {
            if (i != null){
                try { i.close(); }
                catch (IOException ignore) {}
            }
        }
    }
}
