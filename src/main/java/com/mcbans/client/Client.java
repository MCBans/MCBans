package com.mcbans.client;

import com.mcbans.client.response.BanResponse;
import com.mcbans.domain.Ban;
import com.mcbans.utils.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

public class Client {
    private Socket client;
    private OutputStream outputStream;
    private InputStream inputStream;
    public Client(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
        client = new Socket("api.mcbans.com", 8082);
        outputStream = client.getOutputStream();
        inputStream = client.getInputStream();
        registerClient(apiKey);
    }

    private void sendCommand(MCBansCommands command, long referenceId) throws IOException {
        outputStream.write(command.getValue()); // register server to session command
        outputStream.write(ByteBuffer.allocate(8).putLong(referenceId).array());
        outputStream.flush();
    }
    private void sendCommand(MCBansCommands command) throws IOException {
        sendCommand(command, -1);
    }
    private Command getCommand(InputStream inputStream) throws IOException {
        byte[] commandId = new byte[1];
        inputStream.read(commandId);
        System.out.println(commandId[0]);
        byte[] referenceId = new byte[8];
        inputStream.read(referenceId);
        long referenceIdLong = ByteBuffer.wrap(referenceId).getLong();
        if(referenceIdLong!=-1)
            System.out.println("Reference Id: "+referenceIdLong);
        return new Command(referenceIdLong, commandId[0]);
    }

    private void registerClient(String apiKey) throws BadApiKeyException, IOException, TooLargeException {
        sendCommand(MCBansCommands.SessionRegister);
        WriteToOutputStream.writeString(outputStream, apiKey); // apikey
        WriteToOutputStream.writeString(outputStream, ""); // secretKey
        Command command = getCommand(inputStream);
        switch(command.getCommand()){
            case -126:
                throw new BadApiKeyException(ReadFromInputStream.readString(inputStream, 128) + ": "+apiKey);
            case 126:
                System.out.println("Acknowledge received");
                break;
        }
    }

    public BanResponse banStatusByPlayerName(String playerName, String ipAddress, boolean loginRequest) throws IOException, ClassNotFoundException, TooLargeException {
        sendCommand(MCBansCommands.BanStatusByPlayerName);
        WriteToOutputStream.writeString(outputStream, playerName);
        WriteToOutputStream.writeString(outputStream, ipAddress);
        WriteToOutputStream.writeBoolean(outputStream, loginRequest);
        return banStatusResponseHandler();
    }
    public BanResponse banStatusByPlayerUUID(String playerUUID, String ipAddress, boolean loginRequest) throws IOException, ClassNotFoundException, TooLargeException {
        sendCommand(MCBansCommands.BanStatusByPlayerUUID);
        WriteToOutputStream.writeString(outputStream, playerUUID);
        WriteToOutputStream.writeString(outputStream, ipAddress);
        WriteToOutputStream.writeBoolean(outputStream, loginRequest);
        return banStatusResponseHandler();
    }
    public BanResponse banStatusResponseHandler() throws IOException, ClassNotFoundException, TooLargeException {
        Command command = getCommand(inputStream);
        switch (command.getCommand()){
            case 10:
                String uuid = ReadFromInputStream.readString(inputStream, 32);
                String name = ReadFromInputStream.readString(inputStream, 128);
                List<Ban> bans = ObjectSerializer.deserialize(ReadFromInputStream.readByteArray(inputStream, 1024*25)); // 25 KB
                double reputation = ReadFromInputStream.readDouble(inputStream);
                Ban ban = ObjectSerializer.deserialize(ReadFromInputStream.readByteArray(inputStream, 1024)); // 1KB
                boolean mcbansStaff = ReadFromInputStream.readBoolean(inputStream);
                return new BanResponse(uuid, name, bans, reputation, ban, mcbansStaff);
            case -126:
                return null;
        }
        return null;
    }
    public void close() throws IOException {
        sendCommand(MCBansCommands.SessionClose);
    }
}
