package net.heberling.ismart.mqtt;

import static net.heberling.ismart.mqtt.MqttGatewayTopics.*;
import static net.heberling.ismart.mqtt.RefreshMode.FORCE;
import static net.heberling.ismart.mqtt.RefreshMode.PERIODIC;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.heberling.ismart.asn1.v1_1.entity.VinInfo;
import net.heberling.ismart.mqtt.carconfig.HVACSettings;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleState {

  private static final Logger LOGGER = LoggerFactory.getLogger(VehicleState.class);
  private final GatewayMqttClient client;
  private final String mqttVINPrefix;
  private final Supplier<Clock> clockSupplier;
  private OffsetDateTime lastCarActivity;
  private OffsetDateTime lastSuccessfulRefresh;
  private OffsetDateTime lastCarShutdown;
  private OffsetDateTime lastVehicleMessage;
  // treat HV battery as active, if we don't have any other information
  private boolean hvBatteryActive = true;
  private Long refreshPeriodActive;
  private Long refreshPeriodInactive;
  private Long refreshPeriodAfterShutdown;
  private RefreshMode refreshMode;
  private RefreshMode previousRefreshMode;
  private Integer remoteTemperature;

  public HVACSettings getHvacSettings() {
    return hvacSettings;
  }

  public Integer getRemoteClimateStatus() {
    return remoteClimateStatus;
  }

  private HVACSettings hvacSettings;

  public void setRemoteClimateStatus(Integer remoteClimateStatus) {
    this.remoteClimateStatus = remoteClimateStatus;
  }

  private Integer remoteClimateStatus;

  public VehicleState(GatewayMqttClient client, String mqttAccountPrefix, String vin) {
    this(client, mqttAccountPrefix, vin, Clock::systemDefaultZone);
  }

  protected VehicleState(
      GatewayMqttClient client,
      String mqttAccountPrefix,
      String vin,
      Supplier<Clock> clockSupplier) {
    this.client = client;
    this.mqttVINPrefix = mqttAccountPrefix + "/" + VEHICLES + "/" + vin;
    this.clockSupplier = clockSupplier;
    lastCarShutdown = OffsetDateTime.now(clockSupplier.get());
  }

  public String getMqttVINPrefix() {
    return mqttVINPrefix;
  }

  public void notifyCarActivityTime(OffsetDateTime now, boolean force) throws MqttException {
    // if the car activity changed, notify the channel
    if (lastCarActivity == null || force || lastCarActivity.isBefore(now)) {
      lastCarActivity = now;
      client.publishRetained(
          mqttVINPrefix + "/" + REFRESH_LAST_ACTIVITY, lastCarActivity.toString());
    }
  }

  public void notifyMessage(SaicMessage message) throws MqttException {
    if (lastVehicleMessage == null || message.getMessageTime().isAfter(lastVehicleMessage)) {
      // only publish the latest message
      client.publishRetained(
          mqttVINPrefix + "/" + INFO_LAST_MESSAGE, SaicMqttGateway.toJSON(message));
      lastVehicleMessage = message.getMessageTime();
    }
    // something happened, better check the vehicle state
    notifyCarActivityTime(message.getMessageTime(), false);
  }

  public boolean shouldRefresh() {
    switch (refreshMode) {
      case OFF:
        return false;
      case FORCE:
        setRefreshMode(previousRefreshMode);
        return true;
      case PERIODIC:
      default:
        if (previousRefreshMode == FORCE) {
          previousRefreshMode = null;
          return true;
        }
        if (lastSuccessfulRefresh == null) {
          markSuccessfulRefresh();
          return true;
        }
        if (lastCarActivity.isAfter(lastSuccessfulRefresh)) {
          return true;
        }
        if (hvBatteryActive
            || lastCarShutdown
                .plus(refreshPeriodAfterShutdown, ChronoUnit.SECONDS)
                .isAfter(OffsetDateTime.now(clockSupplier.get()))) {
          return lastSuccessfulRefresh.isBefore(
              OffsetDateTime.now(clockSupplier.get())
                  .minus(refreshPeriodActive, ChronoUnit.SECONDS));
        } else {
          return lastSuccessfulRefresh.isBefore(
              OffsetDateTime.now(clockSupplier.get())
                  .minus(refreshPeriodInactive, ChronoUnit.SECONDS));
        }
    }
  }

  public void setHVBatteryActive(boolean hvBatteryActive) throws MqttException {
    if (!hvBatteryActive && this.hvBatteryActive) {
      this.lastCarShutdown = OffsetDateTime.now(clockSupplier.get());
    }
    this.hvBatteryActive = hvBatteryActive;

    client.publishRetained(
        mqttVINPrefix + "/" + DRIVETRAIN_HV_BATTERY_ACTIVE,
        SaicMqttGateway.toJSON(hvBatteryActive));

    if (hvBatteryActive) {
      notifyCarActivityTime(OffsetDateTime.now(clockSupplier.get()), true);
    }
  }

  public void configure(VinInfo vinInfo) throws MqttException {

    client.publishRetained(
        mqttVINPrefix + "/" + INTERNAL_CONFIGURATION_RAW, vinInfo.getModelConfigurationJsonStr());
    for (String c : vinInfo.getModelConfigurationJsonStr().split(";")) {
      Map<String, String> map = new HashMap<>();
      for (String e : c.split(",")) {
        map.put(e.split(":")[0], e.split(":")[1]);
      }
      client.publishRetained(
          mqttVINPrefix + "/" + INFO_CONFIGURATION + "/" + map.get("code"), map.get("value"));
    }
  }

  public void setRefreshPeriodActive(long refreshPeriodActive) {
    if (this.refreshPeriodActive != null && this.refreshPeriodActive != refreshPeriodActive) {

      try {
        client.publishRetained(
            mqttVINPrefix + "/" + REFRESH_PERIOD_ACTIVE, String.valueOf(refreshPeriodActive));
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message.", e);
      }
    }
    this.refreshPeriodActive = refreshPeriodActive;
  }

  public void setRefreshPeriodInactive(long refreshPeriodInactive) {
    if (this.refreshPeriodInactive != null && this.refreshPeriodInactive != refreshPeriodInactive) {

      try {
        client.publishRetained(
            mqttVINPrefix + "/" + REFRESH_PERIOD_INACTIVE, String.valueOf(refreshPeriodInactive));

      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message.", e);
      }
    }
    this.refreshPeriodInactive = refreshPeriodInactive;
  }

  public void setRefreshMode(RefreshMode refreshMode) {
    if (this.refreshMode != null && this.refreshMode != refreshMode) {
      LOGGER.info("Setting refresh mode to {}", refreshMode.getStringValue());

      if (refreshMode != FORCE) {
        // never send force mode to MQTT.
        // If we get restarted while force mode is active, the configuration from MQTT
        // feature would enable force mode permanently and polling of the car never stops
        try {
          client.publishRetained(mqttVINPrefix + "/" + REFRESH_MODE, refreshMode.getStringValue());
        } catch (MqttException e) {
          throw new MqttGatewayException("Error publishing message. ", e);
        }
      }
    }
    this.previousRefreshMode = this.refreshMode;
    this.refreshMode = refreshMode;
  }

  public RefreshMode getRefreshMode() {
    return this.refreshMode;
  }

  public void markSuccessfulRefresh() {
    this.lastSuccessfulRefresh = OffsetDateTime.now(clockSupplier.get());
  }

  public void setRefreshPeriodAfterShutdown(long refreshPeriodAfterShutdown) {
    if (this.refreshPeriodAfterShutdown != null
        && this.refreshPeriodAfterShutdown != refreshPeriodAfterShutdown) {

      try {
        client.publishRetained(
            mqttVINPrefix + "/" + REFRESH_PERIOD_INACTIVE_GRACE,
            String.valueOf(refreshPeriodAfterShutdown));
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message.", e);
      }
    }
    this.refreshPeriodAfterShutdown = refreshPeriodAfterShutdown;
  }

  public void setRemoteTemperature(Integer remoteTemperature) {
    remoteTemperature = hvacSettings.normalizeTemperature(remoteTemperature);
    if (this.remoteTemperature != null && this.remoteTemperature != remoteTemperature) {
      try {
        client.publishRetained(
            mqttVINPrefix + "/" + CLIMATE_REMOTE_TEMPERATURE, String.valueOf(remoteTemperature));
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message.", e);
      }
    }
    this.remoteTemperature = remoteTemperature;
  }

  public boolean isComplete() {
    return refreshPeriodActive != null
        && refreshPeriodInactive != null
        && refreshPeriodAfterShutdown != null
        && refreshMode != null
        && remoteTemperature != null;
  }

  public void configureMissing() {
    if (refreshPeriodActive == null) {
      setRefreshPeriodActive(30L);
    }
    if (refreshPeriodInactive == null) {
      setRefreshPeriodInactive(86400L);
    }
    if (refreshPeriodAfterShutdown == null) {
      setRefreshPeriodAfterShutdown(600L);
    }
    if (refreshMode == null) {
      setRefreshMode(PERIODIC);
    }
    if (remoteTemperature == null) {
      setRemoteTemperature(22);
    }
  }

  public void configure(String topic, MqttMessage message) {
    switch (topic) {
      case REFRESH_MODE:
        RefreshMode.get(message.toString())
            .ifPresentOrElse(
                this::setRefreshMode,
                () -> {
                  throw new MqttGatewayException("Unsupported payload " + message);
                });
        break;
      case REFRESH_PERIOD_ACTIVE:
        try {
          long value = Long.parseLong(message.toString());
          setRefreshPeriodActive(value);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        break;
      case REFRESH_PERIOD_INACTIVE:
        try {
          long value = Long.parseLong(message.toString());
          setRefreshPeriodInactive(value);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        break;
      case REFRESH_PERIOD_INACTIVE_GRACE:
        try {
          long value = Long.parseLong(message.toString());
          setRefreshPeriodAfterShutdown(value);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        break;
      case CLIMATE_REMOTE_TEMPERATURE:
        try {
          int temperature = Integer.parseInt(message.toString());
          setRemoteTemperature(temperature);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        if (Objects.nonNull(remoteClimateStatus) && remoteClimateStatus > 0) {
          // TODO send ACC command to car
        }
        break;
      default:
        throw new MqttGatewayException("Unsupported topic " + topic);
    }
  }

  public int getRemoteTemperature() {
    return remoteTemperature;
  }

  public void setHvacSettings(HVACSettings hvacSettings) {
    this.hvacSettings = hvacSettings;
  }
}
