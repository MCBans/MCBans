package com.mcbans.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ReadFromInputStream {
    public static String readString(InputStream inputStream, long maxLength) throws IOException, TooLargeException {
        byte[] data = new byte[4];
        inputStream.read(data);
        int dataLength = Long.valueOf(ByteBuffer.wrap(data).getInt()).intValue();
        if(dataLength>maxLength){
            throw new TooLargeException();
        }
        data = new byte[dataLength];
        inputStream.read(data);
        return new String(data, StandardCharsets.UTF_8);
    }
    public static long readLong(InputStream inputStream) throws IOException {
        byte[] data = new byte[8];
        inputStream.read(data);
        return Long.valueOf(ByteBuffer.wrap(data).getLong()).longValue();
    }
    public static boolean readBoolean(InputStream inputStream) throws IOException {
        byte[] data = new byte[1];
        inputStream.read(data);
        return (data[0] == (byte)1)?true:false;
    }
    public static int readInt(InputStream inputStream) throws IOException {
        byte[] data = new byte[8];
        inputStream.read(data);
        return Long.valueOf(ByteBuffer.wrap(data).getLong()).intValue();
    }
    public static double readDouble(InputStream inputStream) throws IOException {
        byte[] data = new byte[8];
        inputStream.read(data);
        return ByteBuffer.wrap(data).getDouble();
    }
    public static byte[] readByteArray(InputStream inputStream, long maxLength) throws IOException, TooLargeException {
        byte[] data = new byte[8];
        inputStream.read(data);
        int dataLength = Long.valueOf(ByteBuffer.wrap(data).getLong()).intValue();
        if(dataLength>maxLength){
            throw new TooLargeException();
        }
        data = new byte[dataLength];
        inputStream.read(data);
        return data;
    }

    /*
    * Used to transfer byte data from incoming socket to output stream
    * @return length of byte array
     */
    public static long readInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream, long maxLength) throws IOException, TooLargeException {
        byte[] data = new byte[8];
        inputStream.read(data);
        long dataLength = ByteBuffer.wrap(data).getLong();
        if(dataLength>maxLength){
            throw new TooLargeException();
        }
        long leftToRead = dataLength;
        data = new byte[1024*8];
        while(leftToRead>0){
            if(leftToRead<1024*8){
                data = new byte[Long.valueOf(leftToRead).intValue()];
            }
            leftToRead -= inputStream.read(data);
            outputStream.write(data);
            outputStream.flush();
        }
        return dataLength;
    }
}
