package com.mcbans.client;

import com.mcbans.client.response.BanResponse;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.*;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class BanStatusClient extends Client{
  public BanStatusClient(Client c) {
    super(c);
  }

  public BanStatusClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
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
    Command command = getCommand();


    switch (command.getCommand()){
      case 10:
        String uuid = ReadFromInputStream.readString(getInputStream(), 32, false);
        String name = ReadFromInputStream.readString(getInputStream(), 128, false);
        List<Ban> bans = ObjectSerializer.deserialize(ReadFromInputStream.readByteArrayToStream(getInputStream(), 1024*25, false)); // 25 KB
        double reputation = ReadFromInputStream.readDouble(getInputStream(), false);
        Ban ban = ObjectSerializer.deserialize(ReadFromInputStream.readByteArrayToStream(getInputStream(), 1024, false)); // 1KB
        boolean mcbansStaff = ReadFromInputStream.readBoolean(getInputStream(), false);
        return new BanResponse(uuid, name, bans, reputation, ban, mcbansStaff);
      case -126:
        return null;
    }
    return null;
  }
}
