package net.heberling.ismart.java.rest;

public class EncryptionUtils {
  private EncryptionUtils() {}

  public static String calculateRequestVerification(
      String resourcePath,
      long sendDate,
      String tenant,
      String contentType,
      String bodyEncrypted,
      String token) {
    String str9 = resourcePath + tenant + token + APIConfig.USER_TYPE;
    String a2 = HashUtils.md5(str9);
    String str10 = sendDate + APIConfig.CONTENT_ENCRYPTED + contentType;
    String a3 = HashUtils.md5(a2 + str10);
    String str11 =
        resourcePath
            + tenant
            + token
            + APIConfig.USER_TYPE
            + sendDate
            + APIConfig.CONTENT_ENCRYPTED
            + contentType
            + bodyEncrypted;
    String a5 = HashUtils.md5(a3 + sendDate);
    if (!StringUtils.isEmpty(a5) && !StringUtils.isEmpty(str11)) {
      return MacUtils.hmacSha256(a5.getBytes(), str11);
    }
    return "";
  }

  public static String decryptResponse(String timeStamp, String contentType, String cipherText) {
    String str4 = timeStamp + APIConfig.CONTENT_ENCRYPTED + contentType;
    String a2 = StringUtils.isEmpty(str4) ? "" : HashUtils.md5(str4);
    String hashedTimeStamp = HashUtils.md5(timeStamp);
    if (!StringUtils.isEmpty(cipherText)) {
      return AESUtils.decrypt(cipherText, a2, hashedTimeStamp);
    }
    return "";
  }

  public static String calculateResponseVerification(String str, String str2, String str3) {
    String str4 = str + APIConfig.CONTENT_ENCRYPTED + str2;
    String a2 = StringUtils.isEmpty(str4) ? "" : HashUtils.md5(str4);
    String str5 = str + APIConfig.CONTENT_ENCRYPTED + str2 + str3;
    String a4 = HashUtils.md5(a2 + str);
    if (!StringUtils.isEmpty(a4) && !StringUtils.isEmpty(str5)) {

      return MacUtils.hmacSha256(a4.getBytes(), str5);
    }

    return "";
  }

  public static final String BASE_URL_P = "https://gateway-mg-eu.soimt.com/api.app/v1/";

  public static String encryptRequest(
      String url, long time, String tenant, String token, String body, String contentType) {

    String sendDate = String.valueOf(time);
    // tenant
    String replace = !StringUtils.isEmpty(url) ? url.replace(BASE_URL_P, "/") : "";
    String encryptedBody = "";
    if (!StringUtils.isEmpty(body)) {
      String sb3 =
          HashUtils.md5(replace + tenant + token + APIConfig.USER_TYPE)
              + sendDate
              + APIConfig.CONTENT_ENCRYPTED
              + contentType;
      String a2 = HashUtils.md5(sb3);
      String a3 = HashUtils.md5(sendDate);
      if (!StringUtils.isEmpty(body) && !StringUtils.isEmpty(a2) && !StringUtils.isEmpty(a3)) {
        encryptedBody = AESUtils.encrypt(body, a2, a3);
      }
    }

    if (encryptedBody == null) {
      encryptedBody = "";
    }
    return encryptedBody;
  }

  public static String decryptRequest(
      String url, long time, String tenant, String token, String body, String contentType) {

    String timeStamp = String.valueOf(time);
    String resourcePath = !StringUtils.isEmpty(url) ? url.replace(BASE_URL_P, "/") : "";
    if (!StringUtils.isEmpty(body)) {
      String sb3 =
          HashUtils.md5(resourcePath + tenant + token + APIConfig.USER_TYPE)
              + timeStamp
              + APIConfig.CONTENT_ENCRYPTED
              + contentType;
      String a2 = HashUtils.md5(sb3);
      String timeStampHash = HashUtils.md5(timeStamp);
      if (!StringUtils.isEmpty(body)
          && !StringUtils.isEmpty(a2)
          && !StringUtils.isEmpty(timeStampHash)) {
        return AESUtils.decrypt(body, a2, timeStampHash);
      }
    }
    return null;
  }
}
