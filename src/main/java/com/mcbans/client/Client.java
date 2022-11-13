package com.mcbans.client;

import com.mcbans.domain.models.client.Ban;
import com.mcbans.plugin.MCBans;
import com.mcbans.utils.Command;
import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;
import com.mcbans.utils.WriteToOutputStream;
import com.mcbans.utils.encryption.EncryptedInputStream;
import com.mcbans.utils.encryption.EncryptedOutputStream;
import com.mcbans.utils.encryption.EncryptionSettings;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

import static com.mcbans.utils.encryption.EncryptionSettings.generateKey;

public class Client {
    private Socket client;
    public static final PublicKey PUBLIC_KEY = EncryptionSettings.publicKey(Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAslqo7hYX+lajbicjTPbB8fRF3BQ42t8cbuY2iLOTf8CEV6wiisq5w4moGdp6d/HqIa9wLch2t5TCVNswOzOFyuZFxIVcsuMCUUCtRVrAR6yHIWrMoMnaiXVzEBY8Gpbt0+JTw9OTYHLvbPLWOSlBv1Jh/IytKaeiNxB+CzXdA4+ILqA23O2sm1yiGnVMBGBIfX4Tnuk7G12937pz8fSl+9TqUCa5dIXkbd2XE6TGh4X7J0wfz0atqwrpoTQ+S0NdmZvGqd/V3YA6U0DiwyM7/huMVppo+m1z0vhrAHArUinIgNsEpWLdBHg7IPyC7QHOJV27y8Rm0oj1O/X/vCjQFQIDAQAB"));
    private OutputStream outputStream;
    private InputStream inputStream;

    public static class ResponseHandler {
        public void bans(List<Ban> bans){}
        public void err(String error){}
        public void ack(){}
        public void partial(long total, long current){}
    }
    public static Client cast(Client client) {
        return new Client(client);
    }
    public Client(Client c) {
        setClient(c.getClient());

        setInputStream(c.getInputStream());
        setOutputStream(c.getOutputStream());
    }
    public Client(String apiKey) throws IOException, BadApiKeyException, TooLargeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        if(System.getenv().containsKey("OVERRIDE_API") && System.getenv().containsKey("OVERRIDE_PORT")){
            client = new Socket(System.getenv("OVERRIDE_API"), Integer.parseInt(System.getenv("OVERRIDE_PORT")));
        }else{
            client = new Socket("api.v4.direct.mcbans.com", 8082);
        }
        client.setKeepAlive(true);
        client.setSoTimeout(5000);
        setOutputStream(client.getOutputStream());
        setInputStream(client.getInputStream());
        if(MCBans.encryptAPI){
            encryptConnection();
        }
        registerClient(apiKey);
    }

    void sendCommand(ServerMCBansCommands command, long referenceId) throws IOException {
        WriteToOutputStream.writeByte(outputStream, command.getValue()); // register server to session command
        WriteToOutputStream.writeLong(outputStream, referenceId);
    }
    void sendCommand(ServerMCBansCommands command) throws IOException {
        sendCommand(command, -1);
    }
    Command getCommand() throws IOException {
        byte commandId = ReadFromInputStream.readByte(getInputStream(), false);
        long referenceIdLong = ReadFromInputStream.readLong(getInputStream(), false);
        return new Command(referenceIdLong, commandId);
    }
    void encryptConnection() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, TooLargeException {
        KeyPair keyPair = generateKey();
        sendCommand(ServerMCBansCommands.EncryptConnection);

        setOutputStream(new EncryptedOutputStream(getOutputStream(), PUBLIC_KEY));
        setInputStream(new EncryptedInputStream(getInputStream(), keyPair.getPrivate()));

        WriteToOutputStream.writeByteArray(getOutputStream(), keyPair.getPublic().getEncoded()); // client public key
        Command c = getCommand();
        switch (c.getCommand()){
        case 126:
            //System.out.println("connection encrypted!");
            break;
        case 124:
            String errorMessage = ReadFromInputStream.readString(getInputStream(), 255, false);
            //System.out.println(errorMessage);
            break;
        }
    }

    void registerClient(String apiKey) throws BadApiKeyException, IOException, TooLargeException {
        sendCommand(ServerMCBansCommands.SessionRegister);
        WriteToOutputStream.writeString(getOutputStream(), apiKey); // apikey
        WriteToOutputStream.writeString(getOutputStream(), "secret"); // secretKey
        Command command = getCommand();
        switch(command.getCommand()){
            case -126:
                throw new BadApiKeyException(ReadFromInputStream.readString(getInputStream(), 128, false) + ": "+apiKey);
            case 126:
                //System.out.println("registration successful");
                break;
        }
    }

    public void verifyConnection() throws IOException, TooLargeException {
        sendCommand(ServerMCBansCommands.VerifyConnection);
        Command command = getCommand();
        switch(command.getCommand()) {
            case 126:
                //System.out.println("Acknowledge received");
                break;
        }
    }


    public void close() throws IOException {
        sendCommand(ServerMCBansCommands.SessionClose);
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
