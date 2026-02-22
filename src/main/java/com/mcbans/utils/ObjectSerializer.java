package com.mcbans.utils;


import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ObjectSerializer {

  /**
   * ObjectInputStream that only permits a fixed set of classes to be
   * deserialized.  Any class not on the allowlist causes an
   * InvalidClassException, preventing gadget-chain attacks.
   */
  private static final class FilteredObjectInputStream extends ObjectInputStream {

    private static final Set<String> ALLOWED = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "com.mcbans.domain.models.client.Ban",
            "com.mcbans.domain.models.client.Player",
            "com.mcbans.domain.models.client.Server",
            "java.util.ArrayList",
            "java.util.HashMap",
            "java.util.Date",
            "java.lang.String",
            "java.lang.Long",
            "java.lang.Double"
        )));

    FilteredObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
        throws IOException, ClassNotFoundException {
      String name = desc.getName();
      // Allow primitive arrays ([B, [I, [J, …) and multi-dimensional variants.
      if (name.matches("^\\[+[BCDFIJSZ]$")) {
        return super.resolveClass(desc);
      }
      // Strip JVM array notation (e.g. "[Lcom.example.Foo;" → "com.example.Foo").
      String className = name.replaceAll("^\\[+L", "").replaceAll(";$", "");
      if (!ALLOWED.contains(className)) {
        throw new InvalidClassException(
            "Unauthorized deserialization attempt blocked: " + name);
      }
      return super.resolveClass(desc);
    }
  }

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
      in = new FilteredObjectInputStream(bis);
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
