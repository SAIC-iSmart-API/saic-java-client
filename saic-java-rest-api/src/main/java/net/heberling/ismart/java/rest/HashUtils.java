package net.heberling.ismart.java.rest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
  public static final String MD5 = "MD5";
  public static final String SHA_1 = "SHA1";
  public static final String SHA_256 = "SHA-256";

  public static String md5(String str) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(MD5);
      messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
      byte[] digest = messageDigest.digest();
      return HexUtils.bytesToHex(digest);
    } catch (NoSuchAlgorithmException e2) {
      throw new RuntimeException(e2);
    }
  }

  public static String sha1(String str) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(SHA_1);
      messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
      return HexUtils.bytesToHex(messageDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static String sha256(String str) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
      messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
      return HexUtils.bytesToHex(messageDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
