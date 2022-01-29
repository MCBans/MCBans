package com.mcbans.plugin.request;

import com.mcbans.client.BadApiKeyException;
import com.mcbans.client.BanLookupClient;
import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.plugin.util.Util;
import com.mcbans.utils.TooLargeException;
import org.bukkit.ChatColor;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.BanLookupCallback;

import java.io.IOException;

public class BanLookupRequest extends BaseRequest<BanLookupCallback> {
  private int banID;

  public BanLookupRequest(final MCBans plugin, final BanLookupCallback callback, final int banID) {
    super(plugin, callback);
    this.banID = banID;
  }

  @Override
  protected void execute() {
    if (callback.getSender() != null) {
      log.info(callback.getSender().getName() + " has performed a ban lookup for ID " + banID + "!");
    }

    try {
      Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
      BanLookupClient.cast(client).lookupBan(banID, new BanLookupClient.DataReceived() {
        @Override
        public void received(Ban ban) {
          callback.success(ban);
        }

        @Override
        public void error(String message) {
          callback.error(message);
        }
      });
      ConnectionPool.release(client);
    } catch (IOException e) {
      if (plugin.getConfigs().isDebug())
        e.printStackTrace();
    } catch (BadApiKeyException | ClassNotFoundException e) {
      if (plugin.getConfigs().isDebug())
        e.printStackTrace();
    } catch (TooLargeException e) {
      if (plugin.getConfigs().isDebug())
        e.printStackTrace();
    } catch (InterruptedException e) {
      if (plugin.getConfigs().isDebug())
        e.printStackTrace();
    } catch (NullPointerException ex) {
      ActionLog.getInstance().severe("Unable to reach MCBans API.");
      callback.error(ChatColor.RED + "Unable to reach MCBans API.");
      if (plugin.getConfigs().isDebug())
        ex.printStackTrace();
    } catch (Exception ex) {
      callback.error("Unknown Error: " + ex.getMessage());
      if (plugin.getConfigs().isDebug())
        ex.printStackTrace();
    }
  }
}
