package com.mcbans.client;

import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

import java.io.IOException;

public class UnBanIpClient extends Client{
  public UnBanIpClient(Client c) {
    super(c);
  }
  public UnBanIpClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public static UnBanIpClient cast(Client client) {
    return new UnBanIpClient(client);
  }

  public void unBanIp(String ip, ResponseHandler responseHandler) throws IOException, TooLargeException {
    sendCommand(ServerMCBansCommands.UnBanIp);
    WriteToOutputStream.writeString(getOutputStream(), ip);
    Command c = getCommand(getInputStream());
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
}
