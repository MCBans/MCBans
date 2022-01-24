package com.mcbans.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WriteToOutputStream {
    static Gson gson = new Gson();
    public static void writeString(OutputStream outputStream, String stringVal) throws IOException {
        byte[] value = new byte[0];
        if(stringVal!=null) {
            value = stringVal.getBytes(StandardCharsets.UTF_8);
        }
        byte[] contentLength = ByteBuffer.allocate(4).putInt(value.length).array();
        outputStream.write(contentLength);
        outputStream.write(value);
    }
    public static void writeLong(OutputStream outputStream, long longVal) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putLong(longVal).array());
    }
    public static void writeInt(OutputStream outputStream, int intVal) throws IOException {
        outputStream.write(ByteBuffer.allocate(4).putInt(intVal).array());
    }
    public static void writeDouble(OutputStream outputStream, double doubleVal) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putDouble(doubleVal).array());
    }
    public static void writeBoolean(OutputStream outputStream, boolean boolValue) throws IOException {
        byte[] boolByte = new byte[1];
        boolByte[0] = (byte)(boolValue?1:0);
        outputStream.write(boolByte);
    }
    public static void writeByte(OutputStream outputStream, byte byteVal) throws IOException {
        byte[] byteArray = new byte[1];
        byteArray[0] = byteVal;
        outputStream.write(byteArray);
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
        }
    }
    public static void writeObjectAsJSONByte(OutputStream outputStream, Object obj) throws IOException {
        byte[] data = gson.toJson(obj).getBytes(StandardCharsets.UTF_8);
        writeByteArray(outputStream, data);
    }
    public static void writeByteArray(OutputStream outputStream, byte[] array) throws IOException {
        outputStream.write(ByteBuffer.allocate(8).putLong(Long.valueOf(array.length)).array());
        outputStream.write(array);
    }
}
