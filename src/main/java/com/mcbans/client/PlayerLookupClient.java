package com.mcbans.client;

import com.mcbans.domain.models.client.Ban;
import com.mcbans.domain.models.client.Player;
import com.mcbans.utils.*;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PlayerLookupClient extends Client {
  public PlayerLookupClient(Client c) {
    super(c);
  }

  public PlayerLookupClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    super(apiKey);
  }
  public static PlayerLookupClient cast(Client client) {
    return new PlayerLookupClient(client);
  }

  public interface DataReceived{
    void received(Player player, List<Ban> bans, Double rep);
    void error(String message);
  }

  public void lookupPlayer(String playerName, DataReceived dataReceived) throws IOException, ClassNotFoundException, TooLargeException {
    if(playerName==null || playerName.length()>16 || playerName.length()<3){
      dataReceived.error("Not valid player name.");
      return;
    }
    sendCommand(ServerMCBansCommands.PlayerLookup);
    WriteToOutputStream.writeString(this.getOutputStream(), playerName);
    Command c = getCommand();
    switch(c.getCommand()){
      case 124:
        dataReceived.error(ReadFromInputStream.readString(getInputStream(), 255, false));
        break;
      case 70:
        int length = Long.valueOf(ReadFromInputStream.readLong(getInputStream(), false)).intValue();
        Player player = ObjectSerializer.deserialize(ReadFromInputStream.readByteData(getInputStream(), length, false));
        List<Ban> bans = null;
        Double rep = null;
        if(ReadFromInputStream.readBoolean(getInputStream(), false)){
          length = Long.valueOf(ReadFromInputStream.readLong(getInputStream(), false)).intValue();
          bans = ObjectSerializer.deserialize(ReadFromInputStream.readByteData(getInputStream(), length, false));
          rep = ReadFromInputStream.readDouble(getInputStream(), false);
        }
        dataReceived.received(player, bans, rep);
        break;
    }
  }
}
