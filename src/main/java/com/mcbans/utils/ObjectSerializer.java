package com.mcbans.utils;


import java.io.*;

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

}
