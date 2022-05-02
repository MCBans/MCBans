package com.mcbans.plugin.callBacks;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import com.mcbans.client.BadApiKeyException;
import com.mcbans.client.Client;
import com.mcbans.client.ConnectionPool;
import com.mcbans.client.InformationCallbackClient;
import com.mcbans.domain.models.client.Plugin;
import com.mcbans.plugin.request.JsonHandler;
import com.mcbans.utils.TooLargeException;
import org.bukkit.ChatColor;

import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.plugin.permission.Perms;

import javax.crypto.NoSuchPaddingException;

public class MainCallBack {
  private final MCBans plugin;
  private final ActionLog log;
  public long last_req = 0;

  public MainCallBack(MCBans plugin) {
    this.plugin = plugin;
    log = plugin.getLog();
  }

  public void start() {
    int callBackInterval = ((60 * 1000) * plugin.getConfigs().getCallBackInterval());
    if (callBackInterval < (60 * 1000 * 15)) {
      callBackInterval = (60 * 1000 * 15);
    }
    int finalCallBackInterval = callBackInterval;

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        new Thread(() -> {
          try {
            mainRequest();
            if (plugin.getConfigs().isDebug())
              plugin.getLog().info("Completed information callback.");
          } catch (IOException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (BadApiKeyException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (InterruptedException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (TooLargeException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (NullPointerException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (NoSuchPaddingException e) {
            e.printStackTrace();
          } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
          } catch (InvalidKeyException e) {
            e.printStackTrace();
          }
        }).start();
        plugin.lastCallBack = System.currentTimeMillis() / 1000;
      }
    }, 0, finalCallBackInterval); // repeat every 5 minutes.
  }

  private void mainRequest() throws IOException, BadApiKeyException, InterruptedException, TooLargeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Client c = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
    InformationCallbackClient.cast(c).updateState(
      plugin.getServer().getMaxPlayers(),
      plugin.getServer().getOnlinePlayers().stream().map(p -> p.getUniqueId().toString().replaceAll("-", "").toLowerCase()).collect(Collectors.toList()),
      plugin.getDescription().getVersion(),
      plugin.getServer().getVersion(),
      plugin.getServer().getBukkitVersion(),
      plugin.getServer().getOnlineMode(),
      plugin.getServer().getName()
    );
    /*Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).map(plugin -> new Plugin(
      plugin.getName(),
      plugin.getDescription().getVersion(),
      plugin.getDescription().getAuthors(),
      plugin.getDescription().getPermissions().stream().map(p -> p.getName()).collect(Collectors.toList())
    )).collect(Collectors.toList());*/
    //plugin.getServer().getPluginManager().getPermissions().stream().map(permission -> permission.getName())
    ConnectionPool.release(c);
  }
    /*private String playerList(){
		StringBuilder playerList=new StringBuilder();
		for(Player player: MCBans.getServer().getOnlinePlayers()){
			if(playerList.length()>0){
				playerList.append(",");
			}
			playerList.append(player.getName());
		}
		return playerList.toString();
	}*/
}