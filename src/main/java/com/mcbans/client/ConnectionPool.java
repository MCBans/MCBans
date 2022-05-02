package com.mcbans.client;

import com.mcbans.plugin.MCBans;
import com.mcbans.utils.TooLargeException;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {
  private static int connections = 0;
  private static Queue<Client> clients = new LinkedBlockingDeque<>();
  private static Map<Client, Timer> keepAliveTimer = new HashMap<>();

  public static Client getConnection(String ApiKey) throws IOException, BadApiKeyException, TooLargeException, InterruptedException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Timer timer;
    Client client = clients.poll();
    if (client != null) {
      if ((timer = keepAliveTimer.getOrDefault(client, null)) != null) {
        timer.cancel();
      }
      return client;
    }
    if (connections < 10) {
      return new Client(ApiKey, MCBans.encryptAPI);
    }
    connections++;
    while ((client = clients.poll()) == null) {
      Thread.sleep(100);
    }
    if ((timer = keepAliveTimer.getOrDefault(client, null)) != null) {
      timer.cancel();
    }
    return client;
  }

  public static void release(Client client) {
    keepAliveTimer.put(client, new Timer());
    keepAliveTimer.get(client).scheduleAtFixedRate(new TimerTask() {
      public void run() {
        new Thread(() -> {
          if (clients.remove(client)) { // only run if it hasn't been taken out of queue
            try {
              client.verifyConnection();
              clients.add(client);
            } catch (IOException | TooLargeException e) {
              this.cancel();
              connections--;
              try {
                client.close();
              } catch (IOException ioException) {
                ioException.printStackTrace();
              }
            }
          }
        }).start();
      }
    }, 5000, 30000);
    clients.add(client);
  }

}
