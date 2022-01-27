package com.mcbans.client;

import com.mcbans.client.response.BanResponse;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.*;

import java.io.IOException;
import java.util.List;

public class BanStatusClient extends Client{
  public BanStatusClient(Client c) {
    super(c);
  }

  public BanStatusClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public static BanStatusClient cast(Client client) {
    return new BanStatusClient(client);
  }


  public BanResponse banStatusByPlayerName(String playerName, String ipAddress, boolean loginRequest) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanStatusByPlayerName);
    WriteToOutputStream.writeString(getOutputStream(), playerName);
    WriteToOutputStream.writeString(getOutputStream(), ipAddress);
    WriteToOutputStream.writeBoolean(getOutputStream(), loginRequest);
    return banStatusResponseHandler();
  }
  public BanResponse banStatusByPlayerUUID(String playerUUID, String ipAddress, boolean loginRequest) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanStatusByPlayerUUID);
    WriteToOutputStream.writeString(getOutputStream(), playerUUID);
    WriteToOutputStream.writeString(getOutputStream(), ipAddress);
    WriteToOutputStream.writeBoolean(getOutputStream(), loginRequest);
    return banStatusResponseHandler();
  }
  public BanResponse banStatusResponseHandler() throws IOException, ClassNotFoundException, TooLargeException {
    Command command = getCommand(getInputStream());
    switch (command.getCommand()){
      case 10:
        String uuid = ReadFromInputStream.readString(getInputStream(), 32);
        String name = ReadFromInputStream.readString(getInputStream(), 128);
        List<Ban> bans = ObjectSerializer.deserialize(ReadFromInputStream.readByteArrayToStream(getInputStream(), 1024*25)); // 25 KB
        double reputation = ReadFromInputStream.readDouble(getInputStream());
        Ban ban = ObjectSerializer.deserialize(ReadFromInputStream.readByteArrayToStream(getInputStream(), 1024)); // 1KB
        boolean mcbansStaff = ReadFromInputStream.readBoolean(getInputStream());
        return new BanResponse(uuid, name, bans, reputation, ban, mcbansStaff);
      case -126:
        return null;
    }
    return null;
  }
}
