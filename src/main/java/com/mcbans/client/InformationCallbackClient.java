package com.mcbans.client;

import com.mcbans.utils.*;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class InformationCallbackClient extends Client{
  public InformationCallbackClient(Client c) {
    super(c);
  }

  public InformationCallbackClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
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
    WriteToOutputStream.writeInt(getOutputStream(), maxPlayers);
    WriteToOutputStream.writeByteArray(getOutputStream(), ObjectSerializer.serialize(players));
    WriteToOutputStream.writeString(getOutputStream(), mcbansVersion);
    WriteToOutputStream.writeString(getOutputStream(), minecraftVersion);
    WriteToOutputStream.writeString(getOutputStream(), bukkitVersion);
    WriteToOutputStream.writeBoolean(getOutputStream(), onlineMode);
    WriteToOutputStream.writeString(getOutputStream(), serverName);
    Command c = getCommand();
    switch (c.getCommand()){
      case 126:
        //System.out.println("success");
        break;
      case 124:
        String errorMessage = ReadFromInputStream.readString(getInputStream(), 255, false);
        //System.out.println(errorMessage);
        break;
    }
  }
}
