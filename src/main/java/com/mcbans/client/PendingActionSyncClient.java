package com.mcbans.client;

import com.mcbans.utils.TooLargeException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PendingActionSyncClient extends Client{
  public PendingActionSyncClient(Client c) {
    super(c);
  }
  public PendingActionSyncClient(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
    super(apiKey);
  }
  public static PendingActionSyncClient cast(Client client) {
    return new PendingActionSyncClient(client);
  }



  public interface CommandReceived{
    void exec(byte command) throws IOException, TooLargeException;
  }

  public void listenForPending(CommandReceived commandReceived) throws IOException, TooLargeException {

    boolean continueListening = true;
    sendCommand(ServerMCBansCommands.PendingActions);
    do{
      byte[] command = new byte[1];
      getInputStream().read(command);
      getInputStream().read(new byte[8]); // throw referenceId away
      if(command.length>0 && command[0]==(byte)127){
        commandReceived.exec(command[0]);
        continueListening = false;
      }else if(command.length>0){
        commandReceived.exec(command[0]);
      }else{
        continueListening = false;
      }
    }while(continueListening);

  }
}
