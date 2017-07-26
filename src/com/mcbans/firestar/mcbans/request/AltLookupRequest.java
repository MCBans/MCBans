package com.mcbans.firestar.mcbans.request;

import java.util.UUID;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.AltLookupData;
import com.mcbans.firestar.mcbans.callBacks.AltLookupCallback;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;

public class AltLookupRequest extends BaseRequest<AltLookupCallback>{
    private String playerName;
    private UUID playerUUID;
    
    public AltLookupRequest(final MCBans plugin, final AltLookupCallback callback, final UUID playerUUID, final String playerName) {
        super(plugin, callback);

        this.items.put("player_uuid", playerUUID.toString());
        this.items.put("player", playerName);
        this.items.put("exec", "altList");
        
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }
    public AltLookupRequest(final MCBans plugin, final AltLookupCallback callback, final String playerName) {
        super(plugin, callback);

        this.items.put("player", playerName);
        this.items.put("exec", "altList");
        
        this.playerName = playerName;
    }

    @Override
    protected void execute() {
        if (callback.getSender() != null){
            log.info(callback.getSender().getName() + " has looked up " + playerName + "'s alts!");
        }

        JSONObject result = this.request_JOBJ();

        try{
            if (result != null && result.has("result") && result.getString("result").trim().equals("n")){
                callback.error("This server is not premium!");
            }else{
                callback.success(new AltLookupData(playerName, result));
            }
        }
        catch (JSONException ex) {
            if (result.toString().contains("error")) {
                if (result.toString().contains("Server Disabled")) {
                    ActionLog.getInstance().severe("Server Disabled by an MCBans.com Staff Member!");
                    ActionLog.getInstance().severe("To appeal this decision, please file a ticket on forums.mcbans.com!");

                    callback.error("This server is disabled by MCBans Staff!");
                    return;
                }
            }
            ActionLog.getInstance().severe("JSON error while trying to lookup alternate account data!");
            callback.error("JSON Error");
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
        catch (NullPointerException ex) {
            ActionLog.getInstance().severe("Unable to reach MCBans Master server!");
            callback.error(ChatColor.RED + "Unable to reach MCBans Master server!");
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
        catch (Exception ex){
            callback.error("Unknown Error: " + ex.getMessage());
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
    }
}
