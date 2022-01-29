package com.mcbans.plugin.request;

import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.client.PlayerLookupClient;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.plugin.util.Util;
import org.bukkit.ChatColor;
import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.callBacks.LookupCallback;
import com.mcbans.domain.models.client.Player;

import java.util.List;

public class LookupRequest extends BaseRequest<LookupCallback> {
  private String targetName = null;

  public LookupRequest(
    final MCBans plugin,
    final LookupCallback callback,
    final String playerName,
    final String playerUUID,
    final String senderName,
    String senderUUID
  ) {
    super(plugin, callback);
    if (playerUUID!=null && playerUUID.length()==32) {
      targetName = playerUUID;
    } else if(playerName!=null && playerName.length()>=3 && playerName.length()<=16) {
      this.targetName = playerName;
    }
  }

  @Override
  protected void execute() {
    if (callback.getSender() != null) {
      log.info(callback.getSender().getName() + " has looked up the " + targetName + "!");
    }
    if(targetName==null){
      Util.message(callback.getSender(), "Not a valid player name or UUID.");
      return;
    }
    try {
      Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
      PlayerLookupClient.cast(client).lookupPlayer(targetName, new PlayerLookupClient.DataReceived() {
        @Override
        public void received(Player player, List<Ban> bans, Double rep) {
          callback.success(player, bans, rep);
        }

        @Override
        public void error(String message) {
          callback.error(ChatColor.RED + message);
        }
      });
      ConnectionPool.release(client);
    } catch (NullPointerException ex) {
      ActionLog.getInstance().severe("Unable to reach the MCBans API!");
      callback.error(ChatColor.RED + "Unable to reach the MCBans API!");
      if (plugin.getConfigs().isDebug()) {
        ex.printStackTrace();
      }
    } catch (Exception ex) {
      callback.error("Unknown Error: " + ex.getMessage());
      if (plugin.getConfigs().isDebug()) {
        ex.printStackTrace();
      }
    }
  }
}
