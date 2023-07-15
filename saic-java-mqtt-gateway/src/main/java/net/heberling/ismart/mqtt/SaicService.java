package net.heberling.ismart.mqtt;

import static net.heberling.ismart.mqtt.MqttGatewayTopics.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import net.heberling.ismart.Client;
import net.heberling.ismart.asn1.v2_1.Message;
import net.heberling.ismart.asn1.v2_1.MessageCoder;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusReq;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusResp25857;
import net.heberling.ismart.asn1.v3_0.entity.OTA_ChrgMangDataResp;
import org.bn.coders.IASN1PreparedElement;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaicService.class);
  private final URI saicUri;
  private final String mqttVINPrefix;
  private final VehicleState vehicleState;
  private final GatewayMqttClient client;
  private final Supplier<Clock> clockSupplier;

  public SaicService(
      URI saicUri, String mqttVINPrefix, VehicleState vehicleState, GatewayMqttClient client) {
    this(saicUri, mqttVINPrefix, vehicleState, client, Clock::systemDefaultZone);
  }

  protected SaicService(
      URI saicUri,
      String mqttVINPrefix,
      VehicleState vehicleState,
      GatewayMqttClient client,
      Supplier<Clock> clockSupplier) {
    this.saicUri = saicUri;
    this.mqttVINPrefix = mqttVINPrefix;
    this.vehicleState = vehicleState;
    this.client = client;
    this.clockSupplier = clockSupplier;
  }

  public OTA_RVMVehicleStatusResp25857 updateVehicleStatus(
      String uid, String token, String vin, VehicleHandler vehicleHandler)
      throws IOException, MqttException {
    MessageCoder<OTA_RVMVehicleStatusReq> otaRvmVehicleStatusReqMessageCoder =
        new MessageCoder<>(OTA_RVMVehicleStatusReq.class);

    OTA_RVMVehicleStatusReq otaRvmVehicleStatusReq = new OTA_RVMVehicleStatusReq();
    otaRvmVehicleStatusReq.setVehStatusReqType(2);
    net.heberling.ismart.asn1.v2_1.Message<OTA_RVMVehicleStatusReq> vehicleStatusRequestMessage =
        otaRvmVehicleStatusReqMessageCoder.initializeMessage(
            uid, token, vin, "511", 25857, 1, otaRvmVehicleStatusReq);

    String vehicleStatusRequest =
        otaRvmVehicleStatusReqMessageCoder.encodeRequest(vehicleStatusRequestMessage);

    String vehicleStatusResponse =
        Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv21"), vehicleStatusRequest);

    net.heberling.ismart.asn1.v2_1.Message<OTA_RVMVehicleStatusResp25857>
        vehicleStatusResponseMessage =
            new MessageCoder<>(OTA_RVMVehicleStatusResp25857.class)
                .decodeResponse(vehicleStatusResponse);

    // we get an eventId back...
    vehicleStatusRequestMessage
        .getBody()
        .setEventID(vehicleStatusResponseMessage.getBody().getEventID());
    // ... use that to request the data again, until we have it
    while (vehicleStatusResponseMessage.getApplicationData() == null) {

      if (vehicleStatusResponseMessage.getBody().isErrorMessagePresent()) {

        if (vehicleStatusResponseMessage.getBody().getResult() == 2) {
          // TODO: relogn
        }

        throw new MqttGatewayException(
            "Refreshing Vehicle State from SAIC API failed with message: "
                + new String(
                    vehicleStatusResponseMessage.getBody().getErrorMessage(),
                    StandardCharsets.UTF_8));
      }

      vehicleStatusRequestMessage.getBody().setUid(uid);
      vehicleStatusRequestMessage.getBody().setToken(token);

      SaicMqttGateway.fillReserved(vehicleStatusRequestMessage.getReserved());

      vehicleStatusRequest =
          otaRvmVehicleStatusReqMessageCoder.encodeRequest(vehicleStatusRequestMessage);

      vehicleStatusResponse =
          Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv21"), vehicleStatusRequest);

      vehicleStatusResponseMessage =
          new MessageCoder<>(OTA_RVMVehicleStatusResp25857.class)
              .decodeResponse(vehicleStatusResponse);

      LOGGER.debug(
          SaicMqttGateway.toJSON(
              SaicMqttGateway.anonymized(
                  new MessageCoder<>(OTA_RVMVehicleStatusResp25857.class),
                  vehicleStatusResponseMessage)));
    }

    handleVehicleStatusMessage(vehicleStatusResponseMessage);
    return vehicleStatusResponseMessage.getApplicationData();
  }

  private void handleVehicleStatusMessage(
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

    Integer remoteClimateStatus =
        vehicleStatusResponseMessage
            .getApplicationData()
            .getBasicVehicleStatus()
            .getRemoteClimateStatus();

    vehicleState.setRemoteClimateStatus(remoteClimateStatus);
    vehicleState.setHVBatteryActive(isCharging || engineRunning || remoteClimateStatus > 0);

    client.publishRetained(
        mqttVINPrefix
            + "/"
            + INTERNAL
            + "/"
            + vehicleStatusResponseMessage.getBody().getApplicationID()
            + "_"
            + vehicleStatusResponseMessage.getBody().getApplicationDataProtocolVersion()
            + "/json",
        SaicMqttGateway.toJSON(vehicleStatusResponseMessage));

    client.publishRetained(mqttVINPrefix + "/" + DRIVETRAIN_RUNNING, String.valueOf(engineRunning));

    client.publishRetained(mqttVINPrefix + "/" + DRIVETRAIN_CHARGING, String.valueOf(isCharging));

    Integer interiorTemperature =
        vehicleStatusResponseMessage
            .getApplicationData()
            .getBasicVehicleStatus()
            .getInteriorTemperature();
    if (interiorTemperature > -128) {
      client.publishRetained(
          mqttVINPrefix + "/" + CLIMATE_INTERIOR_TEMPERATURE, String.valueOf(interiorTemperature));
    }

    Integer exteriorTemperature =
        vehicleStatusResponseMessage
            .getApplicationData()
            .getBasicVehicleStatus()
            .getExteriorTemperature();
    if (exteriorTemperature > -128) {
      client.publishRetained(
          mqttVINPrefix + "/" + CLIMATE_EXTERIOR_TEMPERATURE, String.valueOf(exteriorTemperature));
    }

    client.publishRetained(
        mqttVINPrefix + "/" + DRIVETRAIN_AUXILIARY_BATTERY_VOLTAGE,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getBatteryVoltage()
                / 10.d));

    client.publishRetained(
        mqttVINPrefix + "/" + LOCATION_POSITION,
        SaicMqttGateway.toJSON(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getGpsPosition()
                .getWayPoint()
                .getPosition()));

    client.publishRetained(
        mqttVINPrefix + "/" + LOCATION_SPEED,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getGpsPosition()
                    .getWayPoint()
                    .getSpeed()
                / 10d));

    client.publishRetained(
        mqttVINPrefix + "/" + LOCATION_HEADING,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getGpsPosition()
                    .getWayPoint()
                    .getHeading()
                / 10d));

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_LOCKED,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getLockStatus()));

    // todo check configuration for available doors

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_DRIVER,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getDriverDoor()));

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_PASSENGER,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getPassengerDoor()));

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_REAR_LEFT,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getRearLeftDoor()));

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_REAR_RIGHT,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getRearRightDoor()));

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_BOOT,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getBootStatus()));

    client.publishRetained(
        mqttVINPrefix + "/" + DOORS_BONNET,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getBonnetStatus()));

    client.publishRetained(
        mqttVINPrefix + "/" + TYRES_FRONT_LEFT_PRESSURE,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getFrontLeftTyrePressure()
                * 4
                / 100d));

    client.publishRetained(
        mqttVINPrefix + "/" + TYRES_FRONT_RIGHT_PRESSURE,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getFrontRrightTyrePressure()
                * 4
                / 100d));

    client.publishRetained(
        mqttVINPrefix + "/" + TYRES_REAR_LEFT_PRESSURE,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getRearLeftTyrePressure()
                * 4
                / 100d));
    client.publishRetained(
        mqttVINPrefix + "/" + TYRES_REAR_RIGHT_PRESSURE,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getRearRightTyrePressure()
                * 4
                / 100d));

    client.publishRetained(
        mqttVINPrefix + "/" + TYRES_REAR_RIGHT_PRESSURE,
        String.valueOf(
            vehicleStatusResponseMessage
                    .getApplicationData()
                    .getBasicVehicleStatus()
                    .getRearRightTyrePressure()
                * 4
                / 100d));

    client.publishRetained(
        mqttVINPrefix + "/" + CLIMATE_REMOTE_CLIMATE_STATE,
        vehicleState.getHvacSettings().toRemoteClimate(remoteClimateStatus));

    client.publishRetained(
        mqttVINPrefix + "/" + CLIMATE_BACK_WINDOW_HEAT,
        String.valueOf(
            vehicleStatusResponseMessage
                .getApplicationData()
                .getBasicVehicleStatus()
                .getRmtHtdRrWndSt()));

    if (vehicleStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getMileage()
        > 0) {
      // sometimes mileage is 0, ignore such values
      client.publishRetained(
          mqttVINPrefix + "/" + DRIVETRAIN_MILEAGE,
          String.valueOf(
              vehicleStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getMileage()
                  / 10.d));

      // if the milage is 0, the electric range is also 0

      client.publishRetained(
          mqttVINPrefix + "/" + DRIVETRAIN_RANGE,
          String.valueOf(
              vehicleStatusResponseMessage
                      .getApplicationData()
                      .getBasicVehicleStatus()
                      .getFuelRangeElec()
                  / 10.d));
    }

    client.publishRetained(
        mqttVINPrefix + "/" + REFRESH_LAST_VEHICLE_STATE,
        OffsetDateTime.now(clockSupplier.get()).toString());
  }

  OTA_ChrgMangDataResp updateChargeStatus(
      String uid, String token, String vin, VehicleHandler vehicleHandler)
      throws IOException, MqttException {
    net.heberling.ismart.asn1.v3_0.MessageCoder<IASN1PreparedElement>
        chargingStatusRequestMessageEncoder =
            new net.heberling.ismart.asn1.v3_0.MessageCoder<>(IASN1PreparedElement.class);

    net.heberling.ismart.asn1.v3_0.Message<IASN1PreparedElement> chargingStatusMessage =
        chargingStatusRequestMessageEncoder.initializeMessage(uid, token, vin, "516", 768, 5, null);

    String chargingStatusRequestMessage =
        chargingStatusRequestMessageEncoder.encodeRequest(chargingStatusMessage);

    LOGGER.debug(
        SaicMqttGateway.toJSON(
            SaicMqttGateway.anonymized(
                chargingStatusRequestMessageEncoder, chargingStatusMessage)));

    String chargingStatusResponse =
        Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv30"), chargingStatusRequestMessage);

    net.heberling.ismart.asn1.v3_0.Message<OTA_ChrgMangDataResp> chargingStatusResponseMessage =
        new net.heberling.ismart.asn1.v3_0.MessageCoder<>(OTA_ChrgMangDataResp.class)
            .decodeResponse(chargingStatusResponse);

    LOGGER.debug(
        SaicMqttGateway.toJSON(
            SaicMqttGateway.anonymized(
                new net.heberling.ismart.asn1.v3_0.MessageCoder<>(OTA_ChrgMangDataResp.class),
                chargingStatusResponseMessage)));

    // we get an eventId back...
    chargingStatusMessage
        .getBody()
        .setEventID(chargingStatusResponseMessage.getBody().getEventID());
    // ... use that to request the data again, until we have it
    while (chargingStatusResponseMessage.getApplicationData() == null) {

      if (chargingStatusResponseMessage.getBody().isErrorMessagePresent()) {
        if (chargingStatusResponseMessage.getBody().getResult() == 2) {
          // TODO: relogn
        }
        LOGGER.error(
            "Refreshing Charging State from SAIC API failed with message: {}",
            new String(
                chargingStatusResponseMessage.getBody().getErrorMessage(), StandardCharsets.UTF_8));
        return null;
      }

      SaicMqttGateway.fillReserved(chargingStatusMessage.getReserved());

      LOGGER.debug(
          SaicMqttGateway.toJSON(
              SaicMqttGateway.anonymized(
                  chargingStatusRequestMessageEncoder, chargingStatusMessage)));

      chargingStatusRequestMessage =
          chargingStatusRequestMessageEncoder.encodeRequest(chargingStatusMessage);

      chargingStatusResponse =
          Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv30"), chargingStatusRequestMessage);

      chargingStatusResponseMessage =
          new net.heberling.ismart.asn1.v3_0.MessageCoder<>(OTA_ChrgMangDataResp.class)
              .decodeResponse(chargingStatusResponse);

      LOGGER.debug(
          SaicMqttGateway.toJSON(
              SaicMqttGateway.anonymized(
                  new net.heberling.ismart.asn1.v3_0.MessageCoder<>(OTA_ChrgMangDataResp.class),
                  chargingStatusResponseMessage)));
    }
    handleChargeStatusMessage(chargingStatusResponseMessage);

    return chargingStatusResponseMessage.getApplicationData();
  }

  private void handleChargeStatusMessage(
      net.heberling.ismart.asn1.v3_0.Message<OTA_ChrgMangDataResp> chargingStatusResponseMessage)
      throws MqttException {

    client.publishRetained(
        mqttVINPrefix
            + "/"
            + INTERNAL
            + "/"
            + chargingStatusResponseMessage.getBody().getApplicationID()
            + "_"
            + chargingStatusResponseMessage.getBody().getApplicationDataProtocolVersion()
            + "/json",
        SaicMqttGateway.toJSON(chargingStatusResponseMessage));

    double current =
        chargingStatusResponseMessage.getApplicationData().getBmsPackCrnt() * 0.05d - 1000.0d;
    client.publishRetained(mqttVINPrefix + "/" + DRIVETRAIN_CURRENT, String.valueOf(current));

    double voltage =
        (double) chargingStatusResponseMessage.getApplicationData().getBmsPackVol() * 0.25d;

    client.publishRetained(mqttVINPrefix + "/" + DRIVETRAIN_VOLTAGE, String.valueOf(voltage));

    int remainingChargingTime = 0;
    if (chargingStatusResponseMessage.getApplicationData().getChargeStatus().getChargingGunState()
        && current < 0) {
      remainingChargingTime =
          chargingStatusResponseMessage.getApplicationData().getChrgngRmnngTime() * 60;
    }
    client.publishRetained(
        mqttVINPrefix + "/" + DRIVETRAIN_REMAINING_CHARGING_TIME,
        String.valueOf(remainingChargingTime));

    double power = current * voltage / 1000d;
    client.publishRetained(mqttVINPrefix + "/" + DRIVETRAIN_POWER, String.valueOf(power));

    client.publishRetained(
        mqttVINPrefix + "/" + DRIVETRAIN_CHARGER_CONNECTED,
        String.valueOf(
            chargingStatusResponseMessage
                .getApplicationData()
                .getChargeStatus()
                .getChargingGunState()));

    client.publishRetained(
        mqttVINPrefix + "/" + DRIVETRAIN_CHARGING_TYPE,
        String.valueOf(
            chargingStatusResponseMessage
                .getApplicationData()
                .getChargeStatus()
                .getChargingType()));

    client.publishRetained(
        mqttVINPrefix + "/" + DRIVETRAIN_SOC,
        String.valueOf(
            chargingStatusResponseMessage.getApplicationData().getBmsPackSOCDsp() / 10d));

    client.publishRetained(
        mqttVINPrefix + "/" + REFRESH_LAST_CHARGE_STATE,
        OffsetDateTime.now(clockSupplier.get()).toString());
  }
}
