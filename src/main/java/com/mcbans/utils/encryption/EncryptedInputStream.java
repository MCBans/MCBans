package com.mcbans.utils.encryption;

import com.mcbans.utils.ReadFromInputStream;
import com.mcbans.utils.TooLargeException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import static com.mcbans.utils.encryption.EncryptionSettings.decryptBytes;

public class EncryptedInputStream extends InputStream {
  InputStream is;
  PrivateKey privateKey;

  public EncryptedInputStream(InputStream is, PrivateKey privateKey) {
    this.is = is;
    this.privateKey = privateKey;
  }

  @Override
  public int read() throws IOException {
    return 0;
  }
  long length = 0;
  long bytesRead = 0;
  long chunks = 0;
  long onChunk = 0;

  boolean debug = false;

  // in buffer
  int position = 0;
  byte[] buffer=null;

  static int dataId = 0;
  @Override
  public int read(byte[] bytes, int byteOffset, int byteLength) throws IOException {
    dataId++;
    if (buffer == null || (position == buffer.length && (onChunk <= chunks || chunks == 0))) {
      try {
        if (chunks == 0 || onChunk == chunks) {
          length = ReadFromInputStream.readLong(is, debug);
          chunks = ReadFromInputStream.readLong(is, debug);
          onChunk = 0;
        }
        if(chunks>0) {
          byte[] encrypted = ReadFromInputStream.readByteArrayToStream(is, 1024 * 25, debug);
          buffer = decryptBytes(encrypted, privateKey);
          position = 0;
          onChunk++;
        }else{
          buffer = null;
          position=0;
        }
      } catch (TooLargeException e) {
        e.printStackTrace();
        throw new IOException(e);
      } catch (NoSuchPaddingException e) {
        e.printStackTrace();
        throw new IOException(e);
      } catch (IllegalBlockSizeException e) {
        e.printStackTrace();
        throw new IOException(e);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        throw new IOException(e);
      } catch (BadPaddingException e) {
        e.printStackTrace();
        throw new IOException(e);
      } catch (InvalidKeyException e) {
        e.printStackTrace();
        throw new IOException(e);
      }
    }
    if (buffer != null && position < buffer.length) {
      int readLength = (byteLength > (buffer.length - position)) ? buffer.length - position : byteLength;
      int i;

      for (i = 0; i < readLength; i++) {
        bytes[byteOffset + i] = buffer[position + i];
      }
      position += i;
      bytesRead += readLength;
      return readLength;
    }
    if (onChunk >= chunks) {
      onChunk = 0;
      chunks = 0;
      position = 0;
      buffer = null;
      return 0;
    }
    return 0;
  }

  public int readFully(byte[] bytes) throws IOException {
    while(length==0 || onChunk<chunks && position<length) {
      dataId++;
      if (buffer == null || (position == buffer.length && (onChunk < chunks || chunks == 0))) {
        try {
          if (chunks == 0) {
            length = ReadFromInputStream.readLong(is, debug);
            chunks = ReadFromInputStream.readLong(is, debug);
            onChunk = 0;
          }
          if(chunks>0) {
            byte[] encrypted = ReadFromInputStream.readByteArrayToStream(is, 1024 * 25, debug);
            buffer = decryptBytes(encrypted, privateKey);
          }
          position = 0;
          onChunk++;
        } catch (TooLargeException e) {
          e.printStackTrace();
          throw new IOException(e);
        } catch (NoSuchPaddingException e) {
          e.printStackTrace();
          throw new IOException(e);
        } catch (IllegalBlockSizeException e) {
          e.printStackTrace();
          throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
          throw new IOException(e);
        } catch (BadPaddingException e) {
          e.printStackTrace();
          throw new IOException(e);
        } catch (InvalidKeyException e) {
          e.printStackTrace();
          throw new IOException(e);
        }
      }
      if (buffer != null && position < buffer.length) {
        int readLength = (bytes.length > buffer.length - position) ? buffer.length - position : bytes.length;
        int i;
        for (i = 0; i < readLength; i++) {
          bytes[Long.valueOf(bytesRead).intValue()+i] = buffer[position + i];
        }
        position = position + i;
        bytesRead += readLength;
      }
      if (onChunk == chunks) {
        onChunk = 0;
        chunks = 0;
        position = 0;
        buffer = null;
        return 0;
      }
    }
//    System.out.println("["+dataId+"][INPUT END LOOP] >> position: "+position);
//    System.out.println("["+dataId+"][INPUT END LOOP] >> length: "+length);
    return position;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public long skip(long n) throws IOException {
    position += n;
    return position;
  }


  // returns the amount available from the wrapped input stream, not accurate for reading total unencrypted bytes.
  @Override
  public int available() throws IOException {
    return is.available();
  }
}
