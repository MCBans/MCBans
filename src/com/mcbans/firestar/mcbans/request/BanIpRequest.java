package com.mcbans.firestar.mcbans.request;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.I18n;
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.callBacks.MessageCallback;
import com.mcbans.firestar.mcbans.events.PlayerIPBanEvent;
import com.mcbans.firestar.mcbans.events.PlayerIPBannedEvent;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Locale;

import static com.mcbans.firestar.mcbans.I18n.localize;

public class BanIpRequest extends BaseRequest<MessageCallback>{
    private String ip;
    private String reason;
    private String issuedBy,issuedByUUID;

    public BanIpRequest(final MCBans plugin, final MessageCallback callback, final String ip, final String reason, final String issuedBy, String issuedByUUID){
        super(plugin, callback);

        this.items.put("exec", "ipBan");
        this.items.put("ip", ip);
        this.items.put("reason", reason);
        this.items.put("admin", issuedBy);
        this.items.put("admin_uuid", issuedByUUID);
        
        this.ip = ip;
        this.reason = reason;
        this.issuedBy = issuedBy;
        this.issuedByUUID = issuedByUUID;
    }

    @Override
    protected void execute() {
        JSONObject response = this.request_JOBJ();
        
        PlayerIPBanEvent ipBanEvent = new PlayerIPBanEvent(ip, issuedBy, issuedByUUID, reason);
        plugin.getServer().getPluginManager().callEvent(ipBanEvent);
        if (ipBanEvent.isCancelled()){
            return;
        }
        issuedBy = ipBanEvent.getSenderName();
        reason = ipBanEvent.getReason();
        
        // Add default bukkit ipban
        if (Util.isValidIP(ip)){
            Bukkit.getServer().banIP(ip);
        }
        
        try {
            if (response != null && response.has("result")){
                final String result = response.getString("result").trim().toLowerCase(Locale.ENGLISH);
                switch(result){
                    case "y":
                        //callback.setMessage(Util.color(msg))
                        callback.setBroadcastMessage(ChatColor.GREEN + localize("ipBanSuccess", I18n.IP, this.ip, I18n.SENDER, this.issuedBy, I18n.REASON, this.reason));
                        callback.success();

                        kickPlayerByIP(this.ip, reason);

                        log.info("IP " + ip + " has been banned [" + reason + "] [" + issuedBy + "]!");
                        plugin.getServer().getPluginManager().callEvent(new PlayerIPBannedEvent(ip, issuedBy, issuedByUUID, reason));
                        break;
                    case "a":
                        // equals("a") if already banned ip
                        callback.error(ChatColor.RED + localize("ipBanAlready", I18n.IP, this.ip, I18n.SENDER, this.issuedBy, I18n.REASON, this.reason));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                        break;
                    case "n":
                        // equals("n") if banning ip is formatted improperly
                        callback.error(ChatColor.RED + localize("invalidIP"));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                        break;
                    case "e":
                        // other error
                        callback.error(ChatColor.RED + localize("invalidIP"));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                        break;
                    default:
                        log.severe("Invalid response result: " + result);
                        break;
                }
            }else{
                callback.error(ChatColor.RED + "MCBans API appears to be down or unreachable!");
            }
        } catch (JSONException ex) {
            if (response.toString().contains("error")) {
                if (response.toString().contains("Server Disabled")) {
                    ActionLog.getInstance().severe("This server has been disabled by MCBans staff.");
                    ActionLog.getInstance().severe("To appeal this decision, please file a ticket at forums.mcbans.com.");

                    callback.error("This server has been disabled by MCBans staff.");
                    return;
                }
            }
            ActionLog.getInstance().severe("A JSON error occurred while trying to parse lookup data.");
            callback.error("An error occurred while parsing JSON data.");
            if (plugin.getConfigs().isDebug()){
                ex.printStackTrace();
            }
        }
    }
    
    private void kickPlayerByIP(final String ip, final String kickReason){
        for (final Player p : Bukkit.getOnlinePlayers()){
            if (ip.equals(p.getAddress().getAddress().getHostAddress())){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> p.kickPlayer(kickReason), 0L);
            }
        }
    }
}