package net.heberling.ismart.java.rest;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

  public static final String AES = "AES";

  public static String decrypt(String cipherText, String hexKey, String hexIV) {
    if (StringUtils.isEmpty(cipherText)
        || StringUtils.isEmpty(hexKey)
        || StringUtils.isEmpty(hexIV)) {
      return null;
    }
    try {
      SecretKeySpec secretKeySpec = new SecretKeySpec(HexUtils.hexToBytes(hexKey), AES);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(HexUtils.hexToBytes(hexIV));
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(2, secretKeySpec, ivParameterSpec);
      return new String(cipher.doFinal(HexUtils.hexToBytes(cipherText)), StandardCharsets.UTF_8);
    } catch (Exception e2) {
      throw new RuntimeException(e2);
    }
  }

  public static String encrypt(String plainText, String hexKey, String hexIV) {
    if (StringUtils.isEmpty(plainText)
        || StringUtils.isEmpty(hexKey)
        || StringUtils.isEmpty(hexIV)) {
      return null;
    }
    try {
      SecretKeySpec secretKeySpec = new SecretKeySpec(HexUtils.hexToBytes(hexKey), AES);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(HexUtils.hexToBytes(hexIV));
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(1, secretKeySpec, ivParameterSpec);
      return HexUtils.bytesToHex(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e2) {
      throw new RuntimeException(e2);
    }
  }
}
