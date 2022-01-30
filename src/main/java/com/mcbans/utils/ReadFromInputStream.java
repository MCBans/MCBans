package com.mcbans.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ReadFromInputStream {
  static Gson gson = new Gson();
  public static String readString(InputStream inputStream, long maxLength) throws IOException, IndexOutOfBoundsException, TooLargeException {
    int dataLength = Long.valueOf(ByteBuffer.wrap(readByteData(inputStream, 4)).getInt()).intValue();
    if (dataLength > maxLength) {
      throw new TooLargeException();
    }
    return new String(readByteData(inputStream, dataLength), StandardCharsets.UTF_8);
  }

  public static long readLong(InputStream inputStream) throws IOException, IndexOutOfBoundsException {
    return Long.valueOf(ByteBuffer.wrap(readByteData(inputStream, 8)).getLong()).longValue();
  }

  public static boolean readBoolean(InputStream inputStream) throws IOException, IndexOutOfBoundsException {
    byte[] data = readByteData(inputStream, 1);
    return (data[0] == (byte) 1) ? true : false;
  }

  public static byte readByte(InputStream inputStream) throws IOException, IndexOutOfBoundsException {
    byte[] data = readByteData(inputStream, 1);
    return data[0];
  }

  public static int readInt(InputStream inputStream) throws IOException, IndexOutOfBoundsException {
    return ByteBuffer.wrap(readByteData(inputStream, 4)).getInt();
  }

  public static double readDouble(InputStream inputStream) throws IOException, IndexOutOfBoundsException, TooLargeException {
    return ByteBuffer.wrap(readByteData(inputStream, 8)).getDouble();
  }

  public static byte[] readByteArrayToStream(InputStream inputStream, long maxLength) throws IOException, IndexOutOfBoundsException, TooLargeException {
    int dataLength = Long.valueOf(ByteBuffer.wrap(readByteData(inputStream, 8)).getLong()).intValue();
    if (dataLength > maxLength) {
      throw new TooLargeException();
    }
    ByteBuffer bb = ByteBuffer.allocate(dataLength);
    int leftToRead = dataLength;
    int readDataChunkSize = 1024 * 8;
    byte[] data = new byte[readDataChunkSize];
    int readData;
    while (leftToRead > 0) {
      if (leftToRead < readDataChunkSize)
        data = new byte[leftToRead];
      readData = inputStream.read(data);
      if(readData==-1)
        return null;
      leftToRead -= readData;
      /*System.out.println("Read: "+readData);
      System.out.println("Expected Total: "+dataLength);
      System.out.println("Left To Read: "+leftToRead);
      System.out.println("=======================");*/
      bb.put(data, 0, readData);
    }
    return bb.array();
  }

  public static byte[] readByteData(InputStream inputStream, int length) throws IOException, IndexOutOfBoundsException {
    ByteBuffer bb = ByteBuffer.allocate(length);
    int leftToRead = length;
    int readDataChunkSize = 1024 * 8;
    byte[] data = new byte[readDataChunkSize];
    int readData;
    while (leftToRead > 0) {
      if (leftToRead < readDataChunkSize)
        data = new byte[leftToRead];
      readData = inputStream.read(data);
      if(readData==-1)
        return null;
      leftToRead -= readData;
      /*System.out.println("Read: "+readData);
      System.out.println("Expected Total: "+length);
      System.out.println("Left To Read: "+leftToRead);
      System.out.println("=======================");*/
      if(data.length<readData)
        return null;
      bb.put(data, 0, readData);
    }
    return bb.array();
  }

  public static <T> T readJSONObject(InputStream inputStream, int maxLength, Class type) throws IOException, TooLargeException {
    byte[] data = readByteArrayToStream(inputStream, maxLength);
    Object obj = gson.fromJson(
      new String(data, "UTF-8"),
      type
    );
    return (T) obj;
  }

  public static <T> T readJSONObject(InputStream inputStream, int maxLength, TypeToken typeToken) throws IOException, TooLargeException {
    byte[] data = readByteArrayToStream(inputStream, maxLength);
    Object obj = gson.fromJson(
      new String(data, "UTF-8"),
      typeToken.getType()
    );
    return (T) obj;
  }

  /*
   * Used to transfer byte data from incoming socket to output stream
   * @return length of byte array
   */
  public static long readInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream, long maxLength) throws IOException, TooLargeException {
    long dataLength = ByteBuffer.wrap(readByteData(inputStream, 8)).getLong();
    if (dataLength > maxLength) {
      throw new TooLargeException();
    }
    long leftToRead = dataLength;
    byte[] data = new byte[1024 * 8];
    while (leftToRead > 0) {
      if (leftToRead < 1024 * 8) {
        data = new byte[Long.valueOf(leftToRead).intValue()];
      }
      leftToRead -= inputStream.read(data);

      outputStream.write(data);
      outputStream.flush();
    }
    return dataLength;
  }
}
