package net.heberling.ismart.mqtt;

import static net.heberling.ismart.mqtt.MqttGatewayTopics.*;
import static net.heberling.ismart.mqtt.RefreshMode.FORCE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import net.heberling.ismart.Client;
import net.heberling.ismart.abrp.ABRP;
import net.heberling.ismart.asn1.v1_1.entity.VinInfo;
import net.heberling.ismart.asn1.v2_1.MessageCoder;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVCReq;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVCStatus25857;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusResp25857;
import net.heberling.ismart.asn1.v2_1.entity.RvcReqParam;
import net.heberling.ismart.asn1.v3_0.Message;
import net.heberling.ismart.asn1.v3_0.entity.OTA_ChrgCtrlReq;
import net.heberling.ismart.asn1.v3_0.entity.OTA_ChrgCtrlStsResp;
import net.heberling.ismart.asn1.v3_0.entity.OTA_ChrgMangDataResp;
import net.heberling.ismart.mqtt.carconfig.DefaultHVACSettings;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(VehicleHandler.class);
  private final URI saicUri;
  private final String uid;
  private final String token;
  private final VinInfo vinInfo;
  private final SaicMqttGateway saicMqttGateway;
  private final GatewayMqttClient client;

  private final VehicleState vehicleState;
  private final DefaultHVACSettings hvacSettings;

  private final SaicService saicService;

  public VehicleHandler(
      SaicMqttGateway saicMqttGateway,
      GatewayMqttClient client,
      URI saicUri,
      String uid,
      String token,
      String mqttAccountPrefix,
      VinInfo vinInfo,
      VehicleState vehicleState) {

    this.saicMqttGateway = saicMqttGateway;
    this.client = client;
    this.saicUri = saicUri;
    this.uid = uid;
    this.token = token;
    this.vinInfo = vinInfo;
    this.vehicleState = vehicleState;
    switch (vinInfo.getSeries()) {
        // TODO which extra cases do we need?
      default:
        this.hvacSettings = new DefaultHVACSettings();
    }
    vehicleState.setHvacSettings(hvacSettings);
    saicService = new SaicService(saicUri, mqttAccountPrefix, vehicleState, client);
  }

  void handleVehicle() throws MqttException, IOException {
    vehicleState.configure(vinInfo);
    // we just got started, force some updates
    OffsetDateTime startTime = OffsetDateTime.now();
    vehicleState.notifyCarActivityTime(startTime, true);
    while (true) {
      if (!vehicleState.isComplete() && OffsetDateTime.now().isAfter(startTime.plusSeconds(10))) {
        vehicleState.configureMissing();
      }
      if (vehicleState.isComplete() && vehicleState.shouldRefresh()) {

        try {

          OTA_RVMVehicleStatusResp25857 vehicleStatus =
              saicService.updateVehicleStatus(uid, token, vinInfo.getVin(), this);

          OTA_ChrgMangDataResp chargeStatus =
              saicService.updateChargeStatus(uid, token, vinInfo.getVin(), this);
          final String abrpApiKey = saicMqttGateway.getAbrpApiKey();
          final String abrpUserToken = saicMqttGateway.getAbrpUserToken(vinInfo.getVin());
          if (abrpApiKey != null && abrpUserToken != null && vehicleStatus != null) {
            String abrpResponse =
                ABRP.updateAbrp(abrpApiKey, abrpUserToken, vehicleStatus, chargeStatus);
            client.publishRetained(
                vehicleState.getMqttVINPrefix() + "/" + INTERNAL_ABRP, abrpResponse);
          }
          if (Objects.isNull(chargeStatus)) {
            updateFallbackChargeStateData(vehicleStatus);
          }
          vehicleState.markSuccessfulRefresh();
          LOGGER.info("Refreshing vehicle status succeeded...");

        } catch (MqttGatewayException e) {
          LOGGER.warn(e.getMessage());
        }

      } else {
        try {
          // car not active, wait a second
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void updateFallbackChargeStateData(OTA_RVMVehicleStatusResp25857 vehicleStatus)
      throws MqttException {
    LOGGER.warn("Extracting SOC from vehicle status as charge state update failed...");

    client.publishRetained(
        vehicleState.getMqttVINPrefix() + "/" + DRIVETRAIN_SOC,
        vehicleStatus.getBasicVehicleStatus().getExtendedData1().toString());
  }

  public void notifyMessage(SaicMessage message) throws MqttException {
    vehicleState.notifyMessage(message);
  }

  private void sendACCommand(byte command, byte temperature)
      throws URISyntaxException,
          ExecutionException,
          InterruptedException,
          TimeoutException,
          MqttException,
          IOException {
    sendCommand(
        (byte) 6,
        new TreeMap<>(
            Map.of(19, new byte[] {command}, 20, new byte[] {temperature}, 255, new byte[] {0})));
  }

  private void sendACBlowingCommand(boolean on)
      throws URISyntaxException,
          ExecutionException,
          InterruptedException,
          TimeoutException,
          MqttException,
          IOException {
    sendCommand(
        (byte) 6,
        new TreeMap<>(
            Map.of(
                19,
                new byte[] {(byte) (on ? 1 : 0)},
                20,
                new byte[] {0},
                22,
                new byte[] {(byte) (on ? 1 : 0)},
                255,
                new byte[] {0})));
  }

  private void sendCommand(byte type, SortedMap<Integer, byte[]> parameter)
      throws URISyntaxException,
          ExecutionException,
          InterruptedException,
          TimeoutException,
          MqttException,
          IOException {
    MessageCoder<OTA_RVCReq> otaRvcReqMessageCoder = new MessageCoder<>(OTA_RVCReq.class);

    // we send a command end expect the car to wake up
    vehicleState.notifyCarActivityTime(OffsetDateTime.now(), false);

    OTA_RVCReq req = new OTA_RVCReq();
    req.setRvcReqType(new byte[] {type});
    if (parameter != null && !parameter.isEmpty()) {
      List<RvcReqParam> params = new ArrayList<>();
      req.setRvcParams(params);

      parameter.forEach(
          (key, value) -> {
            RvcReqParam param = new RvcReqParam();
            param.setParamId(key);
            param.setParamValue(value);
            params.add(param);
          });
    }

    net.heberling.ismart.asn1.v2_1.Message<OTA_RVCReq> sendCommandRequest =
        otaRvcReqMessageCoder.initializeMessage(uid, token, vinInfo.getVin(), "510", 25857, 1, req);

    String sendCommandRequestMessage = otaRvcReqMessageCoder.encodeRequest(sendCommandRequest);

    String sendCommandResponseMessage =
        Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv21"), sendCommandRequestMessage);

    final MessageCoder<OTA_RVCStatus25857> otaRvcStatus25857MessageCoder =
        new MessageCoder<>(OTA_RVCStatus25857.class);
    net.heberling.ismart.asn1.v2_1.Message<OTA_RVCStatus25857> sendCommandReqestMessage =
        otaRvcStatus25857MessageCoder.decodeResponse(sendCommandResponseMessage);

    // ... use that to request the data again, until we have it
    // TODO: check for real errors (result!=0 and/or errorMessagePresent)
    while (sendCommandReqestMessage.getApplicationData() == null) {
      if (sendCommandReqestMessage.getBody().isErrorMessagePresent()) {
        if (sendCommandReqestMessage.getBody().getResult() == 2) {
          // TODO:
          // getBridgeHandler().relogin();
        }
        throw new TimeoutException(
            new String(sendCommandReqestMessage.getBody().getErrorMessage()));
      }
      SaicMqttGateway.fillReserved(sendCommandRequest.getReserved());

      if (sendCommandReqestMessage.getBody().getResult() == 0) {
        // we get an eventId back...
        sendCommandRequest.getBody().setEventID(sendCommandReqestMessage.getBody().getEventID());
      } else {
        // try a fresh eventId
        sendCommandRequest.getBody().setEventID(0);
      }

      sendCommandRequestMessage = otaRvcReqMessageCoder.encodeRequest(sendCommandRequest);

      sendCommandResponseMessage =
          Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv21"), sendCommandRequestMessage);

      sendCommandReqestMessage =
          otaRvcStatus25857MessageCoder.decodeResponse(sendCommandResponseMessage);
    }

    LOGGER.debug(
        "Got SendCommand Response message: {}",
        SaicMqttGateway.toJSON(
            SaicMqttGateway.anonymized(otaRvcStatus25857MessageCoder, sendCommandReqestMessage)));
  }

  private void sendCharging(boolean state)
      throws URISyntaxException,
          ExecutionException,
          InterruptedException,
          TimeoutException,
          MqttException,
          IOException {
    net.heberling.ismart.asn1.v3_0.MessageCoder<OTA_ChrgCtrlReq> otaRvcReqMessageCoder =
        new net.heberling.ismart.asn1.v3_0.MessageCoder<>(OTA_ChrgCtrlReq.class);

    // we send a command end expect the car to wake up
    vehicleState.notifyCarActivityTime(OffsetDateTime.now(), false);

    OTA_ChrgCtrlReq req = new OTA_ChrgCtrlReq();
    req.setTboxV2XReq(0);
    req.setTboxEleccLckCtrlReq(0);
    req.setChrgCtrlReq(state ? 1 : 2);

    Message<OTA_ChrgCtrlReq> sendCommandRequest =
        otaRvcReqMessageCoder.initializeMessage(uid, token, vinInfo.getVin(), "516", 768, 7, req);

    String sendCommandRequestMessage = otaRvcReqMessageCoder.encodeRequest(sendCommandRequest);

    String sendCommandResponseMessage =
        Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv30"), sendCommandRequestMessage);

    final net.heberling.ismart.asn1.v3_0.MessageCoder<OTA_ChrgCtrlStsResp>
        otaRvcStatus25857MessageCoder =
            new net.heberling.ismart.asn1.v3_0.MessageCoder<>(OTA_ChrgCtrlStsResp.class);
    net.heberling.ismart.asn1.v3_0.Message<OTA_ChrgCtrlStsResp> sendCommandReqestMessage =
        otaRvcStatus25857MessageCoder.decodeResponse(sendCommandResponseMessage);

    // ... use that to request the data again, until we have it
    // TODO: check for real errors (result!=0 and/or errorMessagePresent)
    while (sendCommandReqestMessage.getApplicationData() == null) {
      if (sendCommandReqestMessage.getBody().isErrorMessagePresent()) {
        if (sendCommandReqestMessage.getBody().getResult() == 2) {
          // TODO:
          // getBridgeHandler().relogin();
        }
        throw new TimeoutException(
            new String(sendCommandReqestMessage.getBody().getErrorMessage()));
      }
      SaicMqttGateway.fillReserved(sendCommandRequest.getReserved());

      if (sendCommandReqestMessage.getBody().getResult() == 0) {
        // we get an eventId back...
        sendCommandRequest.getBody().setEventID(sendCommandReqestMessage.getBody().getEventID());
      } else {
        // try a fresh eventId
        sendCommandRequest.getBody().setEventID(0);
      }

      sendCommandRequestMessage = otaRvcReqMessageCoder.encodeRequest(sendCommandRequest);

      sendCommandResponseMessage =
          Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mpv30"), sendCommandRequestMessage);

      sendCommandReqestMessage =
          otaRvcStatus25857MessageCoder.decodeResponse(sendCommandResponseMessage);
    }

    LOGGER.debug(
        "Got SendCommand Response message: {}",
        SaicMqttGateway.toJSON(
            SaicMqttGateway.anonymized(otaRvcStatus25857MessageCoder, sendCommandReqestMessage)));
  }

  public void handleMQTTCommand(String topic, MqttMessage message) throws MqttException {
    try {
      if (message.isRetained()) {
        throw new MqttGatewayException("Message may not be retained");
      }
      switch (topic) {
        case DRIVETRAIN_HV_BATTERY_ACTIVE:
          switch (message.toString().toLowerCase()) {
            case "true":
              vehicleState.setHVBatteryActive(true);
              break;
            case "false":
              vehicleState.setHVBatteryActive(false);
              break;
            default:
              throw new MqttGatewayException("Unsupported payload " + message);
          }
          break;
        case DRIVETRAIN_CHARGING:
          switch (message.toString().toLowerCase()) {
            case "true":
              sendCharging(true);
              break;
            case "false":
              sendCharging(false);
              break;
            default:
              throw new MqttGatewayException("Unsupported payload " + message);
          }
          break;
        case CLIMATE_REMOTE_CLIMATE_STATE:
          switch (message.toString().toLowerCase()) {
            case "off":
              sendACCommand((byte) 0, (byte) 0);
              break;
            case "on":
              sendACCommand(
                  (byte) 2, hvacSettings.mapTempToSaicApi(vehicleState.getRemoteTemperature()));
              break;
            case "front":
              sendACCommand(
                  (byte) 5,
                  (byte) hvacSettings.mapTempToSaicApi(vehicleState.getRemoteTemperature()));
            case "blowingOnly":
              sendACBlowingCommand(true);
              break;
            default:
              throw new MqttGatewayException("Unsupported payload " + message);
          }
          break;
        case DOORS_LOCKED:
          switch (message.toString().toLowerCase()) {
            case "true":
              sendCommand((byte) 0x01, new TreeMap<>(Map.of()));
              break;
            case "false":
              sendCommand(
                  (byte) 0x02,
                  new TreeMap<>(
                      Map.of(
                          4,
                          new byte[] {(byte) 0x00},
                          5,
                          new byte[] {(byte) 0x00},
                          6,
                          new byte[] {(byte) 0x00},
                          7,
                          new byte[] {(byte) 0x03},
                          255,
                          new byte[] {(byte) 0x00})));
              break;
            default:
              throw new MqttGatewayException("Unsupported payload " + message);
          }
          break;
        default:
          vehicleState.configure(topic, message);
      }
      client.publish(vehicleState.getMqttVINPrefix() + "/" + topic + "/result", "Success");
      vehicleState.setRefreshMode(FORCE);

    } catch (URISyntaxException
        | ExecutionException
        | InterruptedException
        | TimeoutException
        | IOException
        | MqttGatewayException e) {
      LOGGER.error("Command {} failed with {}.", topic, message, e);

      client.publish(
          vehicleState.getMqttVINPrefix() + "/" + topic + "/result",
          "Command failed. " + e.getMessage());
    }
  }
}
