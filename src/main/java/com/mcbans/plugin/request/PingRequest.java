package com.mcbans.plugin.request;

import com.mcbans.client.BadApiKeyException;
import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.utils.TooLargeException;
import org.bukkit.ChatColor;

import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.MessageCallback;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PingRequest extends BaseRequest<MessageCallback>{
    private long startTime;

    public PingRequest(final MCBans plugin, final MessageCallback callback){
        super(plugin, callback);

        this.items.put("exec", "check");
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void execute() {
        Client client = null;
        try {
            client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
            client.verifyConnection();
            callback.setMessage(ChatColor.GREEN + "API Response Time: " + ((System.currentTimeMillis()) - startTime) + " milliseconds!");
            callback.success();
            ConnectionPool.release(client);
        } catch (IOException e) {
            e.printStackTrace();
            callback.error(ChatColor.RED + "MCBans API appears to be down or unreachable! Please notify MCBans staff!");
        } catch (BadApiKeyException e) {
            e.printStackTrace();
        } catch (TooLargeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}