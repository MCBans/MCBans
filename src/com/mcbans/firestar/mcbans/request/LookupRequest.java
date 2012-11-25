package com.mcbans.firestar.mcbans.request;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.PlayerLookupData;
import com.mcbans.firestar.mcbans.callBacks.LookupCallback;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;

public class LookupRequest extends BaseRequest<LookupCallback>{
    private String targetName;

    public LookupRequest(final MCBans plugin, final LookupCallback callback, final String playerName, final String senderName) {
        super(plugin, callback);

        this.items.put("player", playerName);
        this.items.put("admin", senderName);
        this.items.put("exec", "playerLookup");

        this.targetName = playerName;
    }

    @Override
    protected void execute() {
        if (callback.getSender() != null){
            log.info(callback.getSender().getName() + " has looked up the " + targetName + "!");
        }

        JSONObject result = this.request_JOBJ();

        try{
            callback.success(new PlayerLookupData(targetName, result));
        }
        catch (JSONException ex) {
            if (result.toString().contains("error")) {
                if (result.toString().contains("Server Disabled")) {
                    ActionLog.getInstance().severe("Server Disabled by an MCBans Admin");
                    ActionLog.getInstance().severe("To appeal this decision, please file ticket on support.mcbans.com");

                    callback.error("This server disabled by MCBans Administration.");
                    return;
                }
            }
            ActionLog.getInstance().severe("JSON error while trying to parse lookup data!");
            callback.error("JSON Error");
        }
        catch (NullPointerException ex) {
            ActionLog.getInstance().severe("Unable to reach MCBans Master server!");
            callback.error(ChatColor.RED + "Unable to reach MCBans Master server");
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
