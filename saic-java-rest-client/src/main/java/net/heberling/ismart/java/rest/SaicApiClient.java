package net.heberling.ismart.java.rest;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import net.heberling.ismart.java.rest.api.v1.JsonResponseMessage;
import net.heberling.ismart.java.rest.api.v1.MessageList;
import net.heberling.ismart.java.rest.api.v1.MessageNotificationList;
import net.heberling.ismart.java.rest.api.v1.OauthToken;
import net.heberling.ismart.java.rest.api.v1.VechicleChargingMgmtData;
import net.heberling.ismart.java.rest.api.v1.VehicleCcInfo;
import net.heberling.ismart.java.rest.api.v1.VehicleList;
import net.heberling.ismart.java.rest.api.v1.VehicleLocation;
import net.heberling.ismart.java.rest.api.v1.VehicleStatisticsBasicInfo;
import net.heberling.ismart.java.rest.api.v1.VehicleStatus;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SIAC API HTTP Client implementation using org.eclipse.jetty.client.
 *
 * @author Doug Culnane
 */
public class SaicApiClient {

  private HttpClient httpClient;

  private byte[] deviceId;

  private final Logger logger = LoggerFactory.getLogger(SaicApiClient.class);

  private final String URL_ROOT_V1 = "https://gateway-mg-eu.soimt.com/api.app/v1/";

  public SaicApiClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public OauthToken getOauthToken(String username, String password, String language)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    if (deviceId == null) {
      deviceId = new byte[64];
      new Random().nextBytes(deviceId);
    }
    return sendRequest(
        new OauthToken(),
        URL_ROOT_V1 + "oauth/token",
        HttpMethod.POST,
        "grant_type=password&username="
            + username
            + "&password="
            + HashUtils.sha1(password)
            + "&scope=all&deviceId="
            + HexUtils.bytesToHex(deviceId)
            + "###europecar&deviceType=1&loginType="
            + getLoginType(username)
            + "&language="
            + language,
        "application/x-www-form-urlencoded",
        "",
        "");
  }

  /**
   * "2" if username_is_email else "1",
   *
   * @param username
   * @return
   */
  private int getLoginType(String username) {
    if (username.contains("@")) {
      return 2;
    }
    return 1;
  }

  public VehicleList getVehicleList(String token)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new VehicleList(),
        URL_ROOT_V1 + "vehicle/list",
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public VehicleStatus getVehicleStatus(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new VehicleStatus(),
        URL_ROOT_V1 + "vehicle/status?vin=" + HashUtils.sha256(vin) + "&vehStatusReqType=2",
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public VechicleChargingMgmtData getVehicleChargingMgmtData(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    VechicleChargingMgmtData message1 =
        sendRequest(
            new VechicleChargingMgmtData(),
            URL_ROOT_V1 + "vehicle/charging/mgmtData?vin=" + HashUtils.sha256(vin),
            HttpMethod.GET,
            "",
            "application/json",
            token,
            "");
    if (message1.isSuccess()) {
      return sendRequest(
          new VechicleChargingMgmtData(),
          URL_ROOT_V1 + "vehicle/charging/mgmtData?vin=" + HashUtils.sha256(vin),
          HttpMethod.GET,
          "",
          "application/json",
          token,
          message1.getEventId());
    }
    return message1;
  }

  public VehicleStatisticsBasicInfo getVehicleStatisticsBasicInfo(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new VehicleStatisticsBasicInfo(),
        URL_ROOT_V1 + "vehicle/statisticsBasicInfo?vin=" + HashUtils.sha256(vin),
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public VehicleStatus getVehicleStatusClassified(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new VehicleStatus(),
        URL_ROOT_V1 + "vehicle/status/classified?vin=" + HashUtils.sha256(vin),
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public VehicleCcInfo getVehicleCcInfo(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    VehicleCcInfo message1 =
        sendRequest(
            new VehicleCcInfo(),
            URL_ROOT_V1 + "vehicle/vehicle/ccInfo?vin=" + HashUtils.sha256(vin),
            HttpMethod.GET,
            "",
            "application/json",
            token,
            "");
    if (message1.isSuccess()) {
      return sendRequest(
          new VehicleCcInfo(),
          URL_ROOT_V1 + "vehicle/vehicle/ccInfo?vin=" + HashUtils.sha256(vin),
          HttpMethod.GET,
          "",
          "application/json",
          token,
          message1.getEventId());
    }
    return message1;
  }

  public VehicleLocation getVehicleLocation(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    VehicleLocation message1 =
        sendRequest(
            new VehicleLocation(),
            URL_ROOT_V1 + "vehicle/location?vin=" + HashUtils.sha256(vin),
            HttpMethod.GET,
            "",
            "application/json",
            token,
            "");
    if (message1.isSuccess()) {
      return sendRequest(
          new VehicleLocation(),
          URL_ROOT_V1 + "vehicle/location?vin=" + HashUtils.sha256(vin),
          HttpMethod.GET,
          "",
          "application/json",
          token,
          message1.getEventId());
    }
    return message1;
  }

  public MessageList getMessageList(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new MessageList(),
        URL_ROOT_V1 + "message/list?vin=" + HashUtils.sha256(vin),
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public MessageList getVehicleInfo(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new MessageList(),
        URL_ROOT_V1 + "vehicle/info?vin=" + HashUtils.sha256(vin),
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public JsonResponseMessage getVehicleGeoFence(String token, String vin)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new VehicleStatus(),
        URL_ROOT_V1 + "vehicle/geoFence?vin=" + HashUtils.sha256(vin),
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  public MessageNotificationList getMessageNotificationList(String token)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    return sendRequest(
        new MessageNotificationList(),
        URL_ROOT_V1 + "message/notificationList",
        HttpMethod.GET,
        "",
        "application/json",
        token,
        "");
  }

  private <T extends JsonResponseMessage> T sendRequest(
      T responseMessage,
      String url,
      HttpMethod httpMethod,
      String requestBody,
      String contentType,
      String token,
      String eventId)
      throws IOException, InterruptedException, TimeoutException, ExecutionException {

    Gson gson = new Gson();
    URI endpoint = URI.create(url);

    long appSendDate = System.currentTimeMillis();

    Request request =
        this.httpClient
            .newRequest(endpoint)
            .method(httpMethod)
            .header("tenant-id", APIConfig.TENANT_ID)
            .header("user-type", APIConfig.USER_TYPE)
            .header("app-send-date", "" + appSendDate)
            .header("app-content-encrypted", APIConfig.CONTENT_ENCRYPTED)
            .header("Authorization", APIConfig.PARAM_AUTHENTICATION)
            .header("original-content-type", contentType);

    String encryptedBody = "";
    if (requestBody != null && !requestBody.isBlank()) {
      encryptedBody =
          EncryptionUtils.encryptRequest(
              endpoint.toString(),
              appSendDate,
              APIConfig.TENANT_ID,
              token,
              requestBody,
              contentType);
      request.content(new StringContentProvider(encryptedBody), contentType);
    }

    String replace =
        !StringUtils.isEmpty(endpoint.toString())
            ? endpoint.toString().replace(EncryptionUtils.BASE_URL_P, "/")
            : "";
    request.header(
        "app-verification-string",
        EncryptionUtils.calculateRequestVerification(
            replace, appSendDate, APIConfig.TENANT_ID, contentType, encryptedBody, token));

    if (token != null && !token.isBlank()) {
      request.header("blade-auth", token);
    }
    if (eventId != null && !eventId.isBlank()) {
      request.header("event-id", eventId);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Request: {}", request);
    }

    ContentResponse response = request.send();
    if (response.getHeaders().get("app-content-encrypted") != null
        && response.getHeaders().get("app-content-encrypted").equals("1")) {
      String responseEncripted = response.getContentAsString();

      String decryptedString =
          EncryptionUtils.decryptResponse(
              response.getHeaders().get("app-send-date"),
              response.getHeaders().get("original-content-type"),
              responseEncripted);
      if (logger.isDebugEnabled()) {
        logger.debug("Response: {}", decryptedString);
      }
      responseMessage = (T) gson.fromJson(decryptedString, responseMessage.getClass());
    } else {
      logger.warn("Unexpected Response: {}", response.getContentAsString());
    }

    if (response.getHeaders().get("event-id") != null) {
      responseMessage.setEventId(response.getHeaders().get("event-id"));
    }

    return responseMessage;
  }
}
