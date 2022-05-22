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

  static String getString(byte[] data){
    String out ="";
    for (int i = 0; i < data.length; i++) {
      out += ((!out.equals(""))?", ":"")+(int) data[i];
    }
    return "[ "+out+" ]";
  }

  public static String readString(InputStream inputStream, long maxLength, boolean debug) throws IOException, TooLargeException {
    //if(debug) System.out.println("Trying to read string");
    byte[] data = readByteData(inputStream, 4, debug);
    //if(debug) System.out.println("read from stream, string: "+new String(data)+ " hex: "+HexFormat.of().formatHex(data)+" byteArray: "+getString(data));
    int dataLength = ByteBuffer.wrap(data).getInt();
    if (dataLength > maxLength) {
      throw new TooLargeException();
    }
    return new String(readByteData(inputStream, dataLength, debug), StandardCharsets.UTF_8);
  }

  public static long readLong(InputStream inputStream, boolean debug) throws IOException {
    //if(debug) System.out.println("Trying to read long");
    byte[] data = readByteData(inputStream, 8, debug);
    //if(debug) System.out.println("read from stream, long: "+new String(data)+ " hex: "+HexFormat.of().formatHex(data)+" byteArray: "+getString(data));
    return Long.valueOf(ByteBuffer.wrap(data).getLong()).longValue();
  }

  public static boolean readBoolean(InputStream inputStream, boolean debug) throws IOException {
    //if(debug) System.out.println("Trying to read boolean");
    byte[] data = readByteData(inputStream, 1, debug);
    //if(debug) System.out.println("read from stream, boolean: "+new String(data)+ " hex: "+HexFormat.of().formatHex(data)+" byteArray: "+getString(data));
    return (data[0] == (byte) 1) ? true : false;
  }

  public static byte readByte(InputStream inputStream, boolean debug) throws IOException {
    //if(debug) System.out.println("Trying to read byte");
    byte[] data = readByteData(inputStream, 1, debug);
    //if(debug) System.out.println("read from stream, byte: "+new String(data)+ " hex: "+HexFormat.of().formatHex(data)+" byteArray: "+getString(data));
    return data[0];
  }

  public static int readInt(InputStream inputStream, boolean debug) throws IOException {
    //if(debug) System.out.println("Trying to read int");
    byte[] data = readByteData(inputStream, 4, debug);
    //if(debug) System.out.println("read from stream, int: "+new String(data)+ " hex: "+HexFormat.of().formatHex(data)+" byteArray: "+getString(data));
    return ByteBuffer.wrap(data).getInt();
  }

  public static double readDouble(InputStream inputStream, boolean debug) throws IOException, TooLargeException {
    //if(debug) System.out.println("Trying to read double");
    byte[] data = readByteData(inputStream, 8, debug);
    //if(debug) System.out.println("read from stream, double: "+new String(data)+ " hex: "+HexFormat.of().formatHex(data)+" byteArray: "+getString(data));
    return ByteBuffer.wrap(data).getDouble();
  }

  public static byte[] readByteArrayToStream(InputStream inputStream, long maxLength, boolean debug) throws IOException, TooLargeException {
    //if(debug) System.out.println("Read From "+inputStream.getClass().getName());
    byte[] readBytes = readByteData(inputStream, 8, debug);
    if(readBytes == null)
      return new byte[]{};
    int dataLength = Long.valueOf(ByteBuffer.wrap(readBytes).getLong()).intValue();
    /*System.out.println("dataLength: "+dataLength);
    System.out.println("maxLength: "+maxLength);*/
    if (dataLength > maxLength) {
      throw new TooLargeException();
    }
    ByteBuffer bb = ByteBuffer.allocate(dataLength);
    int leftToRead = dataLength;
    int readDataChunkSize = 1024 * 8;
    return getBytes(inputStream, bb, leftToRead, readDataChunkSize);
  }

  private static byte[] getBytes(InputStream inputStream, ByteBuffer bb, int leftToRead, int readDataChunkSize) throws IOException {
    byte[] data = new byte[readDataChunkSize];
    int readData;
    while (leftToRead > 0) {
      readData = inputStream.read(data, 0, (leftToRead < readDataChunkSize)?leftToRead:readDataChunkSize);
      if(readData>0) {
        /*System.out.println("Read: "+readData);
        System.out.println("Expected Total: "+dataLength);*/
        leftToRead -= readData;
        /*System.out.println("Left To Read: "+leftToRead);
        System.out.println("=======================");*/
        bb.put(data, 0, readData);
      }
    }
    return bb.array();
  }

  public static byte[] readByteData(InputStream inputStream, int length, boolean debug) throws IOException {
    //if(debug) System.out.println("Read from "+inputStream.getClass().getName());

    ByteBuffer bb = ByteBuffer.allocate(length);
    int leftToRead = length;
    int readDataChunkSize = (1024 * 8>length)?length:(1024*8);
    return getBytes(inputStream, bb, leftToRead, readDataChunkSize);
  }

  public static <T> T readJSONObject(InputStream inputStream, int maxLength, Class type, boolean debug) throws IOException, TooLargeException {
    byte[] data = readByteArrayToStream(inputStream, maxLength, debug);
    Object obj = gson.fromJson(
      new String(data, "UTF-8"),
      type
    );
    return (T) obj;
  }

  public static <T> T readJSONObject(InputStream inputStream, int maxLength, TypeToken typeToken, boolean debug) throws IOException, TooLargeException {
    byte[] data = readByteArrayToStream(inputStream, maxLength, debug);
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
  public static long readInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream, long maxLength, boolean debug) throws IOException, TooLargeException {
    long dataLength = ByteBuffer.wrap(readByteData(inputStream, 8, debug)).getLong();
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
