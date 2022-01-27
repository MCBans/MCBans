package com.mcbans.client;

import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

import java.io.IOException;

public class BanClient extends Client{
  public BanClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }

  public BanClient(Client c) {
    super(c);
  }

  public static BanClient cast(Client client) {
    return new BanClient(client);
  }

  public void globalBan(String playerName, String playerUUID, String playerIP, String adminUUID, String reason, ResponseHandler responseHandler) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanPlayer);
    WriteToOutputStream.writeString(getOutputStream(), playerName);
    if(playerUUID!=null){
      WriteToOutputStream.writeBoolean(getOutputStream(), true);
      WriteToOutputStream.writeString(getOutputStream(), playerUUID.replaceAll("-",""));
    }else{
      WriteToOutputStream.writeBoolean(getOutputStream(), false);
    }
    WriteToOutputStream.writeString(getOutputStream(), reason);
    WriteToOutputStream.writeString(getOutputStream(), adminUUID);
    WriteToOutputStream.writeString(getOutputStream(), playerIP);
    WriteToOutputStream.writeByte(getOutputStream(), (byte)1);
    Command c = getCommand(getInputStream());
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
  public void localBan(String playerName, String playerUUID, String playerIP, String adminUUID, String reason, ResponseHandler responseHandler) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanPlayer);
    WriteToOutputStream.writeString(getOutputStream(), playerName);
    if(playerUUID!=null){
      WriteToOutputStream.writeBoolean(getOutputStream(), true);
      WriteToOutputStream.writeString(getOutputStream(), playerUUID.replaceAll("-",""));
    }else{
      WriteToOutputStream.writeBoolean(getOutputStream(), false);
    }
    WriteToOutputStream.writeString(getOutputStream(), reason);
    WriteToOutputStream.writeString(getOutputStream(), adminUUID);
    WriteToOutputStream.writeString(getOutputStream(), playerIP);
    WriteToOutputStream.writeByte(getOutputStream(), (byte)2);
    Command c = getCommand(getInputStream());
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
  public void tempBan(String playerName, String playerUUID, String playerIP, String adminUUID, String reason, String endInTimeString, ResponseHandler responseHandler) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanPlayer);
    WriteToOutputStream.writeString(getOutputStream(), playerName);
    if(playerUUID!=null){
      WriteToOutputStream.writeBoolean(getOutputStream(), true);
      WriteToOutputStream.writeString(getOutputStream(), playerUUID.replaceAll("-",""));
    }else{
      WriteToOutputStream.writeBoolean(getOutputStream(), false);
    }
    WriteToOutputStream.writeString(getOutputStream(), reason);
    WriteToOutputStream.writeString(getOutputStream(), adminUUID);
    WriteToOutputStream.writeString(getOutputStream(), playerIP);
    WriteToOutputStream.writeByte(getOutputStream(), (byte)3);
    WriteToOutputStream.writeString(getOutputStream(), endInTimeString);
    Command c = getCommand(getInputStream());
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
}
