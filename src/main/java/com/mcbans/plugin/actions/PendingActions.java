package com.mcbans.plugin.actions;

import com.mcbans.client.*;
import com.mcbans.plugin.ActionLog;
import com.mcbans.plugin.MCBans;
import com.mcbans.utils.TooLargeException;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

public class PendingActions {
  private final MCBans plugin;
  private final ActionLog log;
  public static boolean currentlyListening = false;

  public PendingActions(MCBans plugin) {
    this.plugin = plugin;
    log = plugin.getLog();
  }

  public void start() {
    int callBackInterval = ((30 * 1000));

    new Timer().scheduleAtFixedRate(new TimerTask() {
      public void run() {
        new Thread(() -> {
          if (currentlyListening) {
            return;
          }
          currentlyListening = true;
          try {
            listen();
            if (plugin.getConfigs().isDebug())
              plugin.getLog().info("Completed pending actions callback.");
          } catch (InterruptedException | TooLargeException | IOException | IndexOutOfBoundsException | NullPointerException | BadApiKeyException e) {
            if (plugin.getConfigs().isDebug())
              e.printStackTrace();
          } catch (NoSuchPaddingException e) {
            e.printStackTrace();
          } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
          } catch (InvalidKeyException e) {
            e.printStackTrace();
          }
          currentlyListening = false;
        }).start();
        plugin.lastPendingActions = System.currentTimeMillis() / 1000;
      }
    }, 0, callBackInterval); // repeat every 5 minutes.
  }

  void listen() throws IOException, BadApiKeyException, InterruptedException, TooLargeException, NullPointerException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Client client = ConnectionPool.getConnection(plugin.getConfigs().getApiKey());
    PendingActionSyncClient pasc = PendingActionSyncClient.cast(client);
    pasc.listenForPending(command -> {
      ClientMCBansCommands clientMCBansCommand = ClientMCBansCommands.get(command);
      if(clientMCBansCommand!=null){
        switch (clientMCBansCommand) {
          case UnbanSync:
            new UnbanSync(plugin).handle(pasc.getInputStream(), pasc.getOutputStream());
            break;
          case END:
            ConnectionPool.release(client);
            break;
        }
      }else{
        client.close();
      }
    });
  }
}
