package com.mcbans.client;

import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

import java.io.IOException;
import java.util.UUID;

public class BanIpClient extends Client{
  public BanIpClient(Client c) {
    super(c);
  }
  public BanIpClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public static BanIpClient cast(Client client) {
    return new BanIpClient(client);
  }

  public void banIp(String ip, String reason, String adminUUID, ResponseHandler responseHandler) throws IOException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanIp);
    WriteToOutputStream.writeString(getOutputStream(), ip);
    WriteToOutputStream.writeString(getOutputStream(), reason);
    WriteToOutputStream.writeString(getOutputStream(), adminUUID);
    Command c = getCommand(getInputStream());
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
  public void banIp(String ip, String reason, UUID adminUUID, ResponseHandler responseHandler) throws IOException, TooLargeException {
    banIp(ip, reason, adminUUID.toString().toLowerCase().replaceAll("-",""), responseHandler);
  }
}
