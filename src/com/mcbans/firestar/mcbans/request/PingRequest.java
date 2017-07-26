package com.mcbans.firestar.mcbans.request;

import org.bukkit.ChatColor;

import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.callBacks.MessageCallback;

public class PingRequest extends BaseRequest<MessageCallback>{
    private long startTime;

    public PingRequest(final MCBans plugin, final MessageCallback callback){
        super(plugin, callback);

        this.items.put("exec", "check");
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void execute() {
        if ("up".equals(request_String())) {
            callback.setMessage(ChatColor.GREEN + "API Response Time: " + ((System.currentTimeMillis()) - startTime) + " milliseconds!");
            callback.success();
        } else {
            callback.error(ChatColor.RED + "MCBans API appears to be down or unreachable! Please notify MCBans staff!");
        }
    }
}