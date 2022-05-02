package com.mcbans.utils.encryption;

import com.mcbans.utils.WriteToOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import static com.mcbans.utils.encryption.EncryptionSettings.CHUNK_SIZE;
import static com.mcbans.utils.encryption.EncryptionSettings.encryptBytes;

public class EncryptedOutputStream extends OutputStream {
  OutputStream outputStream;
  PublicKey publicKey;

  public EncryptedOutputStream(OutputStream outputStream, PublicKey publicKey) {
    this.outputStream = outputStream;
    this.publicKey = publicKey;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[]{(byte)b});
  }

  static int dataId = 0;

  @Override
  public void write(byte[] bytes) throws IOException {
    long length = bytes.length;
    dataId++;
//    System.out.println("------- BUFFER WRITE -------");
//    System.out.println("["+dataId+"][OUTPUT BUFFER LENGTH] >> length: "+length);
    long chunks = Double.valueOf(Math.ceil((double)length/(double)CHUNK_SIZE)).longValue();
//    System.out.println("["+dataId+"][OUTPUT CHUNKS WRITE] >> chunks: "+chunks);
    WriteToOutputStream.writeLong(outputStream, length);
    WriteToOutputStream.writeLong(outputStream, chunks);
    for(int i=0;i<chunks;i++){
      int readThisMuch = Long.valueOf((length - i*CHUNK_SIZE < CHUNK_SIZE)? length - i*CHUNK_SIZE:CHUNK_SIZE).intValue();
//      System.out.println("["+dataId+"][OUTPUT CHUNK WRITE] >> chunk: "+(i+1));
//      System.out.println("["+dataId+"][OUTPUT CHUNK WRITE][UNENCRYPTED] >> chunk length: "+readThisMuch);
      try {
        int readChunkLength = i*CHUNK_SIZE;
//        System.out.println("["+dataId+"][OUTPUT CHUNK WRITE][UNENCRYPTED] >> chunk from: "+readChunkLength+" to: "+(readChunkLength+readThisMuch));
        byte[] encryptedBytes = encryptBytes(Arrays.copyOfRange(bytes, readChunkLength, readChunkLength+readThisMuch), publicKey);
//        System.out.println("["+dataId+"][OUTPUT CHUNK WRITE][ENCRYPTED] >> chunk length: "+encryptedBytes.length);
        WriteToOutputStream.writeByteArray(outputStream, encryptedBytes);
        dataId++;
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      } catch (IllegalBlockSizeException e) {
        e.printStackTrace();
      } catch (BadPaddingException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void flush() throws IOException {
    outputStream.flush();
  }
}
