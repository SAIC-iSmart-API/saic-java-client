package net.heberling.ismart.java.rest;

public class HexUtils {

  public static String bytesToHex(byte[] byteArray) {
    StringBuilder hexStringBuffer = new StringBuilder();
    for (int i = 0; i < byteArray.length; i++) {
      hexStringBuffer.append(byteToHex(byteArray[i]));
    }
    return hexStringBuffer.toString();
  }

  public static String byteToHex(byte num) {
    return String.format("%02x", num);
  }

  public static byte[] hexToBytes(String hexString) {
    if (hexString.length() % 2 == 1) {
      throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
    }

    byte[] bytes = new byte[hexString.length() / 2];
    for (int i = 0; i < hexString.length(); i += 2) {
      bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
    }
    return bytes;
  }

  public static byte hexToByte(String str) {
    return (byte) Integer.parseInt(str, 16);
  }
}
