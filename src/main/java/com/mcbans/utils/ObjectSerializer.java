package com.mcbans.utils;


import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectSerializer {

  public static byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = null;
    byte[] yourBytes;
    try {
      out = new ObjectOutputStream(bos);
      out.writeObject(obj);
      out.flush();
      yourBytes = bos.toByteArray();
    } finally {
      out.close();
      bos.close();
    }
    return yourBytes;
  }

  public static <T> T deserialize(byte[] byteArray) throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
    ObjectInput in = null;
    Object o;
    try {
      in = new ObjectInputStream(bis);
      o = in.readObject();
    } finally {
      if (in != null) {
        bis.close();
        in.close();
      }
    }
    return (T) o;
  }

  public static <T> byte[] serializeUsingBukkit(List<T> objects) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
    dataOutput.writeInt(objects.size());
    try {
      for(T obj: objects){
        dataOutput.writeObject(obj);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    dataOutput.close();
    return outputStream.toByteArray();
  }
  public static <T> List<T> deserializeUsingBukkit(byte[] byteObject) throws IOException, ClassNotFoundException {
    List<T> objects = new ArrayList<>();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(byteObject);
    BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
    int size = dataInput.readInt();
    for (int i = 0; i < size; i++) {
      objects.add(i, (T) dataInput.readObject());
    }
    dataInput.close();
    return objects;
  }
}
