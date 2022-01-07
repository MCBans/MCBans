package com.mcbans.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WriteToOutputStream {
    public static void writeString(OutputStream outputStream, String stringVal) throws IOException {
        byte[] value = stringVal.getBytes(StandardCharsets.UTF_8);
        byte[] contentLength = ByteBuffer.allocate(4).putInt(value.length).array();
        outputStream.write(contentLength);
        outputStream.write(value);
        outputStream.flush();
    }
    public static void writeLong(OutputStream outputStream, long longVal) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putLong(longVal).array());
        outputStream.flush();
    }
    public static void writeInt(OutputStream outputStream, int intVal) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putInt(intVal).array());
        outputStream.flush();
    }
    public static void writeDouble(OutputStream outputStream, double doubleVal) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putDouble(doubleVal).array());
        outputStream.flush();
    }
    public static void writeBoolean(OutputStream outputStream, boolean boolValue) throws IOException {
        byte[] boolByte = new byte[1];
        boolByte[0] = (byte)(boolValue?1:0);
        outputStream.write(boolByte);
        outputStream.flush();
    }
    public static void writeFromInputStream(OutputStream outputStream, InputStream inputStream, long length) throws IOException {
        byte[] data = new byte[1024*8]; // default read size 8 kb
        long leftToRead = length;
        outputStream.write(ByteBuffer.allocate(8).putLong(length).array());
        outputStream.flush();
        while(leftToRead>0){
            if(leftToRead<1024*8){
                data = new byte[Long.valueOf(leftToRead).intValue()];
            }
            leftToRead -= inputStream.read(data);
            outputStream.write(data);
            outputStream.flush();
        }
    }
    public static void writeByteArray(OutputStream outputStream, byte[] array) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putLong(array.length).array());
        outputStream.write(array);
        outputStream.flush();
    }
}
