package com.mcbans.plugin.request;


import java.util.Locale;

import com.mcbans.plugin.events.PlayerIPBanEvent;
import com.mcbans.plugin.events.PlayerIPBannedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.I18n;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.MessageCallback;
import com.mcbans.plugin.org.json.JSONException;
import com.mcbans.plugin.org.json.JSONObject;
import com.mcbans.plugin.util.Util;

import static com.mcbans.plugin.I18n.localize;

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
        new Thread(()->{
            JSONObject response = this.request_JOBJ();
            try {
                if (response != null && response.has("result")){
                    final String result = response.getString("result").trim().toLowerCase(Locale.ENGLISH);
                    if (result.equals("y")){
                        //callback.setMessage(Util.color(msg))
                        callback.setBroadcastMessage(ChatColor.GREEN + localize("ipBanSuccess", I18n.IP, this.ip, I18n.SENDER, this.issuedBy, I18n.REASON, this.reason));
                        callback.success();

                        kickPlayerByIP(this.ip, reason);

                        log.info("IP " + ip + " has been banned [" + reason + "] [" + issuedBy + "]!");
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()->plugin.getServer().getPluginManager().callEvent(new PlayerIPBannedEvent(ip, issuedBy, issuedByUUID, reason)), 0L);
                    }else if (result.equals("a")){
                        // equals("a") if already banned ip
                        callback.error(ChatColor.RED + localize("ipBanAlready", I18n.IP, this.ip, I18n.SENDER, this.issuedBy, I18n.REASON, this.reason));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                    }else if (result.equals("n")){
                        // equals("n") if banning ip is formatted improperly
                        callback.error(ChatColor.RED + localize("invalidIP"));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                    }else if (result.equals("e")){
                        // other error
                        callback.error(ChatColor.RED + localize("invalidIP"));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                    }else{
                        log.severe("Invalid response result: " + result);
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
                ActionLog.getInstance().severe("A JSON error occurred while trying to localize lookup data.");
                callback.error("An error occurred while parsing JSON data.");
                if (plugin.getConfigs().isDebug()){
                    ex.printStackTrace();
                }
            }
        }).start();
    }
    
    private void kickPlayerByIP(final String ip, final String kickReason){
        for (final Player p : Bukkit.getOnlinePlayers()){
            if (ip.equals(p.getAddress().getAddress().getHostAddress())){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()->p.kickPlayer(kickReason), 0L);
            }
        }
    }
}