package com.mcbans.client;

import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class BanClient extends Client{
  public BanClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
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
    Command c = getCommand();
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50, false));
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
    Command c = getCommand();
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50, false));
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
    Command c = getCommand();
    if(c.getCommand()==124){
      responseHandler.err(ReadFromInputStream.readString(getInputStream(), 50, false));
    }else if(c.getCommand()==126){
      responseHandler.ack();
    }
  }
}
