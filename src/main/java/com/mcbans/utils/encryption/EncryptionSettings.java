package com.mcbans.utils.encryption;

import javax.crypto.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionSettings {
  public static final int CHUNK_SIZE = 190;
  private static final String CIPHER_KEY = "RSA";
  private static final String CIPHER_INIT = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
  public static CipherOutputStream encryptStream(OutputStream os, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher c = Cipher.getInstance(CIPHER_INIT);
    c.init(Cipher.ENCRYPT_MODE, key);
    return new CipherOutputStream(os, c);
  }

  public static CipherInputStream decryptStream(InputStream is, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher c = Cipher.getInstance(CIPHER_INIT);
    c.init(Cipher.DECRYPT_MODE, key);
    return new CipherInputStream(is, c);
  }
  public static byte[] encryptBytes(byte[] data, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher c = Cipher.getInstance(CIPHER_INIT);
    c.init(Cipher.ENCRYPT_MODE, key);
    return c.doFinal(data);
  }

  public static byte[] decryptBytes(byte[] data, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher c = Cipher.getInstance(CIPHER_INIT);
    c.init(Cipher.DECRYPT_MODE, key);
    return c.doFinal(data);
  }
  public static KeyPair generateKey() throws NoSuchAlgorithmException {
    SecureRandom secureRandom = new SecureRandom();
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(CIPHER_KEY);

    keyPairGenerator.initialize(2048, secureRandom);
    return keyPairGenerator.generateKeyPair();
  }
  public static PublicKey publicKey(byte[] byteKey){
    try{
      return KeyFactory.getInstance(CIPHER_KEY).generatePublic(new X509EncodedKeySpec(byteKey));
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }

  public static PrivateKey privateKey(byte[] byteKey){
    try{
      return KeyFactory.getInstance(CIPHER_KEY).generatePrivate(new PKCS8EncodedKeySpec(byteKey));
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }
}
