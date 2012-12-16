package com.mcbans.firestar.mcbans.request;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.callBacks.MessageCallback;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.util.Util;

public class BanIpRequest extends BaseRequest<MessageCallback>{
    private String ip;
    private String reason;
    private String issuedBy;

    public BanIpRequest(final MCBans plugin, final MessageCallback callback, final String ip, final String reason, final String issuedBy){
        super(plugin, callback);

        this.items.put("exec", "ipBan");
        this.items.put("ip", ip);
        this.items.put("reason", reason);
        this.items.put("admin", issuedBy);
        
        this.ip = ip;
        this.reason = reason;
        this.issuedBy = issuedBy;
    }

    @Override
    protected void execute() {
        JSONObject result = this.request_JOBJ();
        
        try {
            if (result != null && result.has("result")){
                if (result.getString("result").trim().equals("y")){
                    //callback.setMessage(Util.color(msg))
                    callback.setBroadcastMessage(Util.color("&aIP " + ip + " has been banned by " + issuedBy + ":[" + reason + "]"));
                    callback.success();
                    
                    log.info("IP " + ip + " has been banned [" + reason + "] [" + issuedBy + "]!");
                }else{
                    // always equals("n") if banning ip is formatted improperly
                    callback.error(ChatColor.RED + "Improperly formatted IP address!");
                    log.info(issuedBy + " tried to IPBan " + ip + "!");
                }
            }else{
                callback.error(ChatColor.RED + "API appears to be down!");
            }
        } catch (JSONException ex) {
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
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
    }
}