package com.mcbans.client;

import com.google.common.reflect.TypeToken;
import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

import java.io.IOException;
import java.util.List;

public class BanSyncClient extends Client{
  public BanSyncClient(Client c) {
    super(c);
  }

  public BanSyncClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }

  public static BanSyncClient cast(Client client) {
    return new BanSyncClient(client);
  }

  public void getBanSync(long banId, ResponseHandler responseHandler) throws IOException, ClassNotFoundException, TooLargeException {
    sendCommand(ServerMCBansCommands.BanSync);
    WriteToOutputStream.writeLong(getOutputStream(), banId);
    Command c = getCommand(getInputStream());
    if(c.getCommand()==25) {
      long chunks = ReadFromInputStream.readLong(getInputStream());
      for (long i = 0; i < chunks; i++) {
        responseHandler.bans(
          ReadFromInputStream.readJSONObject(
            getInputStream(),
            1024 * 25,
            new TypeToken<List<Ban>>(){}
          )
        );
        responseHandler.partial(chunks, i+1);
        WriteToOutputStream.writeBoolean(getOutputStream(), true);
      }
    }
  }
}
