package net.heberling.ismart.mqtt;

public class MqttGatewayException extends RuntimeException {
  public MqttGatewayException(String s) {
    super(s);
  }

  public MqttGatewayException(String message, Throwable cause) {
    super(message, cause);
  }
}
