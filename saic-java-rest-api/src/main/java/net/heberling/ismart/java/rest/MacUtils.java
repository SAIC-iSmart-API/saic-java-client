package net.heberling.ismart.java.rest;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MacUtils {
  private static final String HMAC_SHA_256 = "HmacSHA256";

  public static String hmacSha256(byte[] bArr, String str) {
    try {
      Mac mac = Mac.getInstance(HMAC_SHA_256);
      mac.init(new SecretKeySpec(bArr, HMAC_SHA_256));
      return HexUtils.bytesToHex(mac.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }
}
