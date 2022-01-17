package com.mcbans.plugin.request;


import java.io.IOException;
import java.util.Locale;

import com.mcbans.client.*;
import com.mcbans.plugin.events.PlayerBanEvent;
import com.mcbans.plugin.events.PlayerIPBanEvent;
import com.mcbans.plugin.events.PlayerIPBannedEvent;
import com.mcbans.utils.IPTools;
import com.mcbans.utils.TooLargeException;
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
        if (IPTools.validIP(ip)){
            Bukkit.getServer().banIP(ip);
        }
        new Thread(()->{
            try {
                Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
                BanIpClient.cast(client).banIp(ip, reason, issuedByUUID, new Client.ResponseHandler(){
                    @Override
                    public void err(String error) {
                        callback.error(ChatColor.RED + localize("invalidIP"));
                        log.info(issuedBy + " tried to IPBan " + ip + "!");
                    }
                    @Override
                    public void ack() {
                        callback.setBroadcastMessage(ChatColor.GREEN + localize("ipBanSuccess", I18n.IP, ip, I18n.SENDER, issuedBy, I18n.REASON, reason));
                        callback.success();
                        kickPlayerByIP(ip, reason);
                        log.info("IP " + ip + " has been banned [" + reason + "] [" + issuedBy + "]!");
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()->plugin.getServer().getPluginManager().callEvent(new PlayerIPBannedEvent(ip, issuedBy, issuedByUUID, reason)), 0L);
                    }
                });
                ConnectionPool.release(client);
            } catch (IOException e) {
                Util.message(issuedBy, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
                log.warning("Error occurred with local banning. Please report this to an MCBans developer.");
            } catch (BadApiKeyException | TooLargeException | InterruptedException e) {
                e.printStackTrace();
                log.info(issuedBy + " tried to ip ban " + ip + " with the reason [" + reason + "]!");
                Util.message(issuedBy, ChatColor.RED + " MCBans API is down or unreachable. We added a default ban for you. To unban, use /pardon.");
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