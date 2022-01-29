package com.mcbans.client;

import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public class BanLookupClient extends Client {
  public BanLookupClient(Client c) {
    super(c);
  }

  public BanLookupClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public static BanLookupClient cast(Client client) {
    return new BanLookupClient(client);
  }

  public interface DataReceived{
    void received(Ban ban);
    void error(String message);
  }

  public void lookupBan(long banId, DataReceived dataReceived) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanLookup);
    WriteToOutputStream.writeLong(this.getOutputStream(), banId);
    Command c = getCommand(getInputStream());
    switch(c.getCommand()){
      case 124:
        dataReceived.error(ReadFromInputStream.readString(getInputStream(), 255));
        break;
      case 71:
        if(ReadFromInputStream.readBoolean(getInputStream())){
          int length = Long.valueOf(ReadFromInputStream.readLong(getInputStream())).intValue();
          Ban ban = ObjectSerializer.deserialize(ReadFromInputStream.readByteData(getInputStream(), length));
          dataReceived.received(ban);
        }
        break;
    }
  }
}
