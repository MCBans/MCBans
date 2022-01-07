package com.mcbans.client;

import com.mcbans.utils.TooLargeException;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {
  private static int connections = 0;
  private static Queue<Client> clients = new LinkedBlockingDeque<>();
  public static Client getConnection(String ApiKey) throws IOException, BadApiKeyException, TooLargeException, InterruptedException {
    Client client = clients.poll();
    if(client!=null){
      return client;
    }
    if(connections<10){
      return new Client(ApiKey);
    }
    connections++;
    while((client=clients.poll())==null){
      Thread.sleep(100);
    }
    return client;
  }
  public static void release(Client client){
    clients.add(client);
  }
}
