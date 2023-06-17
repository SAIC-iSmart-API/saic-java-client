package net.heberling.ismart.mqtt;

import static net.heberling.ismart.mqtt.MqttGatewayTopics.*;
import static net.heberling.ismart.mqtt.RefreshMode.FORCE;
import static net.heberling.ismart.mqtt.RefreshMode.PERIODIC;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.heberling.ismart.asn1.v1_1.entity.VinInfo;
import net.heberling.ismart.asn1.v2_1.Message;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusResp25857;
import net.heberling.ismart.asn1.v3_0.entity.OTA_ChrgMangDataResp;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleState {

  private static final Logger LOGGER = LoggerFactory.getLogger(VehicleState.class);
  private final IMqttClient client;
  private final String mqttVINPrefix;
  private Supplier<Clock> clockSupplier;
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

  public VehicleState(IMqttClient client, String mqttAccountPrefix, String vin) {
    this(client, mqttAccountPrefix, vin, () -> Clock.systemDefaultZone());
  }

  protected VehicleState(
      IMqttClient client, String mqttAccountPrefix, String vin, Supplier<Clock> clockSupplier) {
    this.client = client;
    this.mqttVINPrefix = mqttAccountPrefix + "/" + VEHICLES + "/" + vin;
    this.clockSupplier = clockSupplier;
    lastCarShutdown = OffsetDateTime.now(clockSupplier.get());
  }

  public String getMqttVINPrefix() {
    return mqttVINPrefix;
  }

  public void handleVehicleStatusMessage(
      Message<OTA_RVMVehicleStatusResp25857> vehicleStatusResponseMessage) throws MqttException {
    boolean engineRunning =
        vehicleStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getEngineStatus()
            == 1;
    boolean isCharging =
        vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .isExtendedData2Present()
            && vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getExtendedData2()
                >= 1;

    final Integer remoteClimateStatus =
        vehicleStatusResponseMessage
            .getApplicationData()
            .getBasicVehicleStatus()
            .getRemoteClimateStatus();

    setHVBatteryActive(isCharging || engineRunning || remoteClimateStatus > 0);

    MqttMessage msg =
        new MqttMessage(
            SaicMqttGateway.toJSON(vehicleStatusResponseMessage).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(
        mqttVINPrefix
            + "/"
            + INTERNAL
            + "/"
            + vehicleStatusResponseMessage.getBody().getApplicationID()
            + "_"
            + vehicleStatusResponseMessage.getBody().getApplicationDataProtocolVersion()
            + "/json",
        msg);

    msg = new MqttMessage(String.valueOf(engineRunning).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_RUNNING, msg);

    msg = new MqttMessage(String.valueOf(isCharging).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_CHARGING, msg);

    Integer interiorTemperature =
        vehicleStatusResponseMessage
            .getApplicationData()
            .getBasicVehicleStatus()
            .getInteriorTemperature();
    if (interiorTemperature > -128) {
      msg = new MqttMessage(String.valueOf(interiorTemperature).getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + CLIMATE_INTERIOR_TEMPERATURE, msg);
    }

    Integer exteriorTemperature =
        vehicleStatusResponseMessage
            .getApplicationData()
            .getBasicVehicleStatus()
            .getExteriorTemperature();
    if (exteriorTemperature > -128) {
      msg = new MqttMessage(String.valueOf(exteriorTemperature).getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + CLIMATE_EXTERIOR_TEMPERATURE, msg);
    }

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getBasicVehicleStatus()
                            .getBatteryVoltage()
                        / 10.d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_AUXILIARY_BATTERY_VOLTAGE, msg);

    msg =
        new MqttMessage(
            SaicMqttGateway.toJSON(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getGpsPosition()
                        .getWayPoint()
                        .getPosition())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + LOCATION_POSITION, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getGpsPosition()
                            .getWayPoint()
                            .getSpeed()
                        / 10d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + LOCATION_SPEED, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getGpsPosition()
                            .getWayPoint()
                            .getHeading()
                        / 10d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + LOCATION_HEADING, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getLockStatus())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_LOCKED, msg);

    // todo check configuration for available doors
    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getDriverDoor())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_DRIVER, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getPassengerDoor())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_PASSENGER, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getRearLeftDoor())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_REAR_LEFT, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getRearRightDoor())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_REAR_RIGHT, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getBootStatus())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_BOOT, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getBonnetStatus())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DOORS_BONNET, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getBasicVehicleStatus()
                            .getFrontLeftTyrePressure()
                        * 4
                        / 100d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + TYRES_FRONT_LEFT_PRESSURE, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getBasicVehicleStatus()
                            .getFrontRrightTyrePressure()
                        * 4
                        / 100d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + TYRES_FRONT_RIGHT_PRESSURE, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getBasicVehicleStatus()
                            .getRearLeftTyrePressure()
                        * 4
                        / 100d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + TYRES_REAR_LEFT_PRESSURE, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                            .getApplicationData()
                            .getBasicVehicleStatus()
                            .getRearRightTyrePressure()
                        * 4
                        / 100d)
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + TYRES_REAR_RIGHT_PRESSURE, msg);

    msg = new MqttMessage(toRemoteClimate(remoteClimateStatus).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + CLIMATE_REMOTE_CLIMATE_STATE, msg);

    msg =
        new MqttMessage(
            String.valueOf(
                    vehicleStatusResponseMessage
                        .getApplicationData()
                        .getBasicVehicleStatus()
                        .getRmtHtdRrWndSt())
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + CLIMATE_BACK_WINDOW_HEAT, msg);

    if (vehicleStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getMileage()
        > 0) {
      // sometimes mileage is 0, ignore such values
      msg =
          new MqttMessage(
              String.valueOf(
                      vehicleStatusResponseMessage
                              .getApplicationData()
                              .getBasicVehicleStatus()
                              .getMileage()
                          / 10.d)
                  .getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + DRIVETRAIN_MILEAGE, msg);

      // if the milage is 0, the electric range is also 0
      msg =
          new MqttMessage(
              String.valueOf(
                      vehicleStatusResponseMessage
                              .getApplicationData()
                              .getBasicVehicleStatus()
                              .getFuelRangeElec()
                          / 10.d)
                  .getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + DRIVETRAIN_RANGE, msg);
    }

    msg =
        new MqttMessage(OffsetDateTime.now(getClock()).toString().getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + REFRESH_LAST_VEHICLE_STATE, msg);
  }

  private static String toRemoteClimate(Integer remoteClimateStatus) {
    switch (remoteClimateStatus) {
      case 0:
        return "off";
      case 2:
        return "on";
      case 5:
        return "front";
      default:
        return "unknown (" + remoteClimateStatus + ")";
    }
  }

  public void handleChargeStatusMessage(
      net.heberling.ismart.asn1.v3_0.Message<OTA_ChrgMangDataResp> chargingStatusResponseMessage)
      throws MqttException {
    MqttMessage msg =
        new MqttMessage(
            SaicMqttGateway.toJSON(chargingStatusResponseMessage).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(
        mqttVINPrefix
            + "/"
            + INTERNAL
            + "/"
            + chargingStatusResponseMessage.getBody().getApplicationID()
            + "_"
            + chargingStatusResponseMessage.getBody().getApplicationDataProtocolVersion()
            + "/json",
        msg);

    double current =
        chargingStatusResponseMessage.getApplicationData().getBmsPackCrnt() * 0.05d - 1000.0d;
    msg = new MqttMessage((String.valueOf(current)).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_CURRENT, msg);

    double voltage =
        (double) chargingStatusResponseMessage.getApplicationData().getBmsPackVol() * 0.25d;
    msg = new MqttMessage((String.valueOf(voltage)).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_VOLTAGE, msg);

    double power = current * voltage / 1000d;
    msg = new MqttMessage((String.valueOf(power)).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_POWER, msg);

    msg =
        new MqttMessage(
            (String.valueOf(
                    chargingStatusResponseMessage
                        .getApplicationData()
                        .getChargeStatus()
                        .getChargingGunState()))
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_CHARGER_CONNECTED, msg);

    msg =
        new MqttMessage(
            (String.valueOf(
                    chargingStatusResponseMessage
                        .getApplicationData()
                        .getChargeStatus()
                        .getChargingType()))
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_CHARGING_TYPE, msg);

    msg =
        new MqttMessage(
            (String.valueOf(
                    chargingStatusResponseMessage.getApplicationData().getBmsPackSOCDsp() / 10d))
                .getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_SOC, msg);

    msg =
        new MqttMessage(OffsetDateTime.now(getClock()).toString().getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + REFRESH_LAST_CHARGE_STATE, msg);
  }

  public void notifyCarActivityTime(OffsetDateTime now, boolean force) throws MqttException {
    // if the car activity changed, notify the channel
    if (lastCarActivity == null || force || lastCarActivity.isBefore(now)) {
      lastCarActivity = now;
      MqttMessage msg =
          new MqttMessage(lastCarActivity.toString().getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + REFRESH_LAST_ACTIVITY, msg);
    }
  }

  public void notifyMessage(SaicMessage message) throws MqttException {
    if (lastVehicleMessage == null || message.getMessageTime().isAfter(lastVehicleMessage)) {
      // only publish the latest message
      MqttMessage msg =
          new MqttMessage(SaicMqttGateway.toJSON(message).getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + INFO_LAST_MESSAGE, msg);
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
                .isAfter(OffsetDateTime.now(getClock()))) {
          return lastSuccessfulRefresh.isBefore(
              OffsetDateTime.now(getClock()).minus(refreshPeriodActive, ChronoUnit.SECONDS));
        } else {
          return lastSuccessfulRefresh.isBefore(
              OffsetDateTime.now(getClock()).minus(refreshPeriodInactive, ChronoUnit.SECONDS));
        }
    }
  }

  public void setHVBatteryActive(boolean hvBatteryActive) throws MqttException {
    if (!hvBatteryActive && this.hvBatteryActive) {
      this.lastCarShutdown = OffsetDateTime.now(getClock());
    }
    this.hvBatteryActive = hvBatteryActive;

    MqttMessage msg =
        new MqttMessage(SaicMqttGateway.toJSON(hvBatteryActive).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + DRIVETRAIN_HV_BATTERY_ACTIVE, msg);

    if (hvBatteryActive) {
      notifyCarActivityTime(OffsetDateTime.now(getClock()), true);
    }
  }

  public void configure(VinInfo vinInfo) throws MqttException {
    MqttMessage msg =
        new MqttMessage(vinInfo.getModelConfigurationJsonStr().getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    msg.setRetained(true);
    client.publish(mqttVINPrefix + "/" + INTERNAL_CONFIGURATION_RAW, msg);
    for (String c : vinInfo.getModelConfigurationJsonStr().split(";")) {
      Map<String, String> map = new HashMap<>();
      for (String e : c.split(",")) {
        map.put(e.split(":")[0], e.split(":")[1]);
      }
      msg = new MqttMessage(map.get("value").getBytes(StandardCharsets.UTF_8));
      msg.setQos(0);
      msg.setRetained(true);
      client.publish(mqttVINPrefix + "/" + INFO_CONFIGURATION + "/" + map.get("code"), msg);
    }
  }

  public void setRefreshPeriodActive(long refreshPeriodActive) {
    if (this.refreshPeriodActive == null || this.refreshPeriodActive != refreshPeriodActive) {
      MqttMessage mqttMessage =
          new MqttMessage(String.valueOf(refreshPeriodActive).getBytes(StandardCharsets.UTF_8));
      try {
        mqttMessage.setRetained(true);
        this.client.publish(this.mqttVINPrefix + "/" + REFRESH_PERIOD_ACTIVE, mqttMessage);
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message: " + mqttMessage, e);
      }
    }
    this.refreshPeriodActive = refreshPeriodActive;
  }

  public void setRefreshPeriodInactive(long refreshPeriodInactive) {
    if (this.refreshPeriodInactive == null || this.refreshPeriodInactive != refreshPeriodInactive) {
      MqttMessage mqttMessage =
          new MqttMessage(String.valueOf(refreshPeriodInactive).getBytes(StandardCharsets.UTF_8));
      try {
        mqttMessage.setRetained(true);
        this.client.publish(this.mqttVINPrefix + "/" + REFRESH_PERIOD_INACTIVE, mqttMessage);
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message: " + mqttMessage, e);
      }
    }
    this.refreshPeriodInactive = refreshPeriodInactive;
  }

  public void setRefreshMode(RefreshMode refreshMode) {
    if (this.refreshMode == null || this.refreshMode != refreshMode) {

      MqttMessage mqttMessage =
          new MqttMessage(refreshMode.getStringValue().getBytes(StandardCharsets.UTF_8));
      try {
        LOGGER.info("Setting refresh mode to {}", refreshMode.getStringValue());
        mqttMessage.setRetained(true);
        this.client.publish(this.mqttVINPrefix + "/" + REFRESH_MODE, mqttMessage);
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message: " + mqttMessage, e);
      }
    }
    this.previousRefreshMode = this.refreshMode;
    this.refreshMode = refreshMode;
  }

  public RefreshMode getRefreshMode() {
    return this.refreshMode;
  }

  public void markSuccessfulRefresh() {
    this.lastSuccessfulRefresh = OffsetDateTime.now(getClock());
  }

  public void setRefreshPeriodAfterShutdown(long refreshPeriodAfterShutdown) {
    if (this.refreshPeriodAfterShutdown == null
        || this.refreshPeriodAfterShutdown != refreshPeriodAfterShutdown) {

      MqttMessage mqttMessage =
          new MqttMessage(
              String.valueOf(refreshPeriodAfterShutdown).getBytes(StandardCharsets.UTF_8));
      try {
        mqttMessage.setRetained(true);
        this.client.publish(this.mqttVINPrefix + "/" + REFRESH_PERIOD_INACTIVE_GRACE, mqttMessage);
      } catch (MqttException e) {
        throw new MqttGatewayException("Error publishing message: " + mqttMessage, e);
      }
    }
    this.refreshPeriodAfterShutdown = refreshPeriodAfterShutdown;
  }

  public boolean isComplete() {
    return refreshPeriodActive != null
        && refreshPeriodInactive != null
        && refreshPeriodAfterShutdown != null
        && refreshMode != null;
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
          long value = Long.valueOf(message.toString());
          setRefreshPeriodActive(value);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        break;
      case REFRESH_PERIOD_INACTIVE:
        try {
          long value = Long.valueOf(message.toString());
          setRefreshPeriodInactive(value);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        break;
      case REFRESH_PERIOD_INACTIVE_GRACE:
        try {
          long value = Long.valueOf(message.toString());
          setRefreshPeriodAfterShutdown(value);
        } catch (NumberFormatException e) {
          throw new MqttGatewayException("Error setting value for payload: " + message);
        }
        break;
      default:
        throw new MqttGatewayException("Unsupported topic " + topic);
    }
  }

  private Clock getClock() {
    return clockSupplier.get();
  }
}
