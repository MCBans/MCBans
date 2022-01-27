package com.mcbans.client;

import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

import java.io.IOException;

public class UnbanClient extends Client{
  public UnbanClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public UnbanClient(Client c) {
    super(c);
  }

  public static UnbanClient cast(Client client) {
    return new UnbanClient(client);
  }

  public void unBan(String playerName, String playerUUID, ResponseHandler responseHandler) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.UnBanPlayer);
    WriteToOutputStream.writeString(getOutputStream(), playerName);
    if(playerUUID!=null && playerUUID.length()==32){
      WriteToOutputStream.writeBoolean(getOutputStream(), true);
      WriteToOutputStream.writeString(getOutputStream(), playerUUID.replaceAll("-",""));
    }else{
      WriteToOutputStream.writeBoolean(getOutputStream(), false);
    }
    Command c = getCommand(getInputStream());
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
}
