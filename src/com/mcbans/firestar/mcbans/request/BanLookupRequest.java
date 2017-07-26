package com.mcbans.firestar.mcbans.request;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.data.BanLookupData;
import com.mcbans.firestar.mcbans.callBacks.BanLookupCallback;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;

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
            log.info(callback.getSender().getName() + " has ban looked up the " + banID + "!");
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
                    ActionLog.getInstance().severe("Server Disabled by MCBans Staff!");
                    ActionLog.getInstance().severe("To appeal this decision, please file ticket on forums.mcbans.com");

                    callback.error("This server has been disabled by MCBans staff!");
                    return;
                }
            }
            ActionLog.getInstance().severe("JSON error while trying to parse ban lookup data!");
            callback.error("JSON Error");
        }
        catch (NullPointerException ex) {
            ActionLog.getInstance().severe("Unable to reach MCBans API!");
            callback.error(ChatColor.RED + "Unable to reach MCBans API!");
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
