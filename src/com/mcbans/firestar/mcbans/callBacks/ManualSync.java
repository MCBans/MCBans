package com.mcbans.firestar.mcbans.callBacks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.org.json.JSONArray;
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
        
        int handled = 0;
        try{
            boolean goNext = true;
            JsonHandler webHandle = new JsonHandler( plugin );
            HashMap<String, String> url_items = new HashMap<String, String>();
            JSONObject response = null;
            
            while(goNext){
                // Prepare request parameters
                url_items.clear();
                url_items.put( "latestSync", String.valueOf(plugin.lastID) );
                url_items.put( "exec", "banSync" );
                
                // Send request, get response
                response = webHandle.hdl_jobj(url_items);                
                if (response == null){
                    Util.message(commandSend, "&cNull json response. Please try again later.");
                    break;
                }
                
                // Handle response
                try {
                    if(response.has("banned")){
                        handled += handleBannedResponses(response.getJSONArray("banned"));
                    }
                    if(response.has("lastid")){
                        long lastId = response.getLong("lastid");
                        if(lastId != 0){
                            plugin.lastID = lastId;
                        }
                    }
                    goNext = response.has("more");
                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }
                
                // Wait 500ms
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {}
            }
        } finally {
            plugin.syncRunning = false;
        }
        Util.message(commandSend, ChatColor.GREEN + " Sync finished, " + handled + " actions!" );
    }
    
    /**
     * Handling banned status responses
     * @param array JSONArray
     * @return Handled record count
     * @throws JSONException
     */
    private int handleBannedResponses(final JSONArray array) throws JSONException{
        if (array == null || array.length() <= 0){
            return 0;
        }
        
        for (int v = 0; v < array.length(); v++) {
            String[] response = array.getString(v).split(";");
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(response[0]);
            if (player == null){
                continue;
            }
            
            if(player.isBanned() && "u".equals(response[1])){
                player.setBanned(false); // unbanning player
            }
            else if(!player.isBanned() && "b".equals(response[1])){
                player.setBanned(true); // banning player
            }
        }        
        return array.length();
    }
}