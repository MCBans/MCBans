package com.mcbans.client;

import com.mcbans.domain.models.client.Ban;
import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;

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

    public static class ResponseHandler {
        public void bans(List<Ban> bans){}
        public void err(String error){}
        public void ack(){}
    }
    public static Client cast(Client client) {
        return new Client(client);
    }
    public Client(Client c) {
        setClient(c.getClient());
        setInputStream(c.getInputStream());
        setOutputStream(c.getOutputStream());
    }
    public Client(String apiKey) throws IOException, BadApiKeyException, TooLargeException {
        client = new Socket("api.v4.mcbans.com", 8082);
        client.setKeepAlive(true);
        client.setSoTimeout(1000);
        outputStream = client.getOutputStream();
        inputStream = client.getInputStream();
        registerClient(apiKey);
    }

    void sendCommand(MCBansCommands command, long referenceId) throws IOException {
        outputStream.write(command.getValue()); // register server to session command
        outputStream.write(ByteBuffer.allocate(8).putLong(referenceId).array());
        outputStream.flush();
    }
    void sendCommand(MCBansCommands command) throws IOException {
        sendCommand(command, -1);
    }
    Command getCommand(InputStream inputStream) throws IOException {
        byte[] commandId = ReadFromInputStream.readByteData(inputStream, 1);
        long referenceIdLong = ByteBuffer.wrap(ReadFromInputStream.readByteData(inputStream, 8)).getLong();
        return new Command(referenceIdLong, commandId[0]);
    }

    void registerClient(String apiKey) throws BadApiKeyException, IOException, TooLargeException {
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

    public void verifyConnection() throws IOException, TooLargeException {
        sendCommand(MCBansCommands.VerifyConnection);
        Command command = getCommand(inputStream);
        switch(command.getCommand()) {
            case 126:
                //System.out.println("Acknowledge received");
                break;
        }
    }


    public void close() throws IOException {
        sendCommand(MCBansCommands.SessionClose);
    }

    public Socket getClient() {
        return client;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
