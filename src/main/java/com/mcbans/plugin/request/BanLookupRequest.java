package com.mcbans.plugin.request;

import org.bukkit.ChatColor;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.api.data.BanLookupData;
import com.mcbans.plugin.callBacks.BanLookupCallback;
import com.mcbans.plugin.org.json.JSONException;
import com.mcbans.plugin.org.json.JSONObject;

public class BanLookupRequest extends BaseRequest<BanLookupCallback>{
    private int banID;

    public BanLookupRequest(final MCBans plugin, final BanLookupCallback callback, final int banID) {
        super(plugin, callback);

        this.items.put("ban", String.valueOf(banID));
        this.items.put("exec", "banLookup");

        this.banID = banID;
    }

    @Override
    protected void execute() {
        if (callback.getSender() != null){
            log.info(callback.getSender().getName() + " has performed a ban lookup for ID " + banID + "!");
        }

        JSONObject result = this.request_JOBJ();

        try{
            callback.success(new BanLookupData(banID, result));
        }
        catch (JSONException ex) {
            if (result.toString().contains("error")) {
                if (result.toString().contains("dne")){
                    callback.error("Ban record not found: " + banID);
                    return;
                }
                else if (result.toString().contains("Server Disabled")) {
                    ActionLog.getInstance().severe("This server has been disabled by MCBans staff.");
                    ActionLog.getInstance().severe("To appeal this decision, please file a ticket at forums.mcbans.com.");

                    callback.error("This server has been disabled by MCBans staff.");
                    return;
                }
            }
            ActionLog.getInstance().severe("A JSON error occurred while trying to localize ban lookup data.");
            callback.error("An error occurred while parsing JSON data.");
        }
        catch (NullPointerException ex) {
            ActionLog.getInstance().severe("Unable to reach MCBans API.");
            callback.error(ChatColor.RED + "Unable to reach MCBans API.");
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
