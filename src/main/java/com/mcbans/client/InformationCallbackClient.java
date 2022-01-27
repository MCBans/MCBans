package com.mcbans.client;

import com.mcbans.utils.*;

import java.io.IOException;
import java.util.List;

public class InformationCallbackClient extends Client{
  public InformationCallbackClient(Client c) {
    super(c);
  }

  public InformationCallbackClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }

  public static InformationCallbackClient cast(Client client) {
    return new InformationCallbackClient(client);
  }

  public void updateState(
    int maxPlayers,
    List<String> players,
    String mcbansVersion,
    String minecraftVersion,
    String bukkitVersion,
    boolean onlineMode,
    String serverName) throws IOException, TooLargeException {
    sendCommand(ServerMCBansCommands.InformationCallback);
    WriteToOutputStream.writeInt(getClient().getOutputStream(), maxPlayers);
    WriteToOutputStream.writeByteArray(getClient().getOutputStream(), ObjectSerializer.serialize(players));
    WriteToOutputStream.writeString(getClient().getOutputStream(), mcbansVersion);
    WriteToOutputStream.writeString(getClient().getOutputStream(), minecraftVersion);
    WriteToOutputStream.writeString(getClient().getOutputStream(), bukkitVersion);
    WriteToOutputStream.writeBoolean(getClient().getOutputStream(), onlineMode);
    WriteToOutputStream.writeString(getClient().getOutputStream(), serverName);
    Command c = getCommand(getInputStream());
    switch (c.getCommand()){
      case 126:
        System.out.println("success");
        break;
      case 124:
        String errorMessage = ReadFromInputStream.readString(getInputStream(), 255);
        System.out.println(errorMessage);
        break;
    }
  }
}
