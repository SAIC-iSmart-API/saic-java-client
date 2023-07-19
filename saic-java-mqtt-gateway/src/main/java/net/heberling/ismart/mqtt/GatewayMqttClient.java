package net.heberling.ismart.mqtt;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.*;

public class GatewayMqttClient {

  private final IMqttClient client;

  public GatewayMqttClient(URI mqttUri, String mqttUser, char[] mqttPassword) {
    String publisherId = UUID.randomUUID().toString();
    try {
      client =
        new MqttClient(mqttUri.toString(), publisherId, null) {
          @Override
          public void close() throws MqttException {
            Thread.dumpStack();
            disconnect();
            super.close(true);
          }
        };
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);
      options.setConnectionTimeout(10);
      if (mqttUser != null) {
        options.setUserName(mqttUser);
      }
      if (mqttPassword != null) {
        options.setPassword(mqttPassword);
      }
      client.connect(options);
    } catch (MqttException e) {
      throw new MqttGatewayException("Error initializing mqtt client.", e);
    }
  }

  public void publish(String topic, String message) throws MqttException {
    MqttMessage msg = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(false);
    client.publish(topic, msg);
  }

  public void publishRetained(String topic, String message) throws MqttException {
    MqttMessage msg = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(topic, msg);
  }

  public void setCallback(MqttCallback callback) {
    client.setCallback(callback);
  }

  public void subscribe(String topic) throws MqttException {
    client.subscribe(topic);
  }
}
