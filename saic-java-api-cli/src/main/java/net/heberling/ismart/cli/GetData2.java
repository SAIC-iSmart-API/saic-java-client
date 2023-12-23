package net.heberling.ismart.cli;

import com.owlike.genson.GensonBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.heberling.ismart.rest.APIConfig;
import net.heberling.ismart.rest.EncryptionUtils;
import net.heberling.ismart.rest.HashUtils;
import net.heberling.ismart.rest.HexUtils;
import net.heberling.ismart.rest.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class GetData2 {
  public static void main(String[] args) throws IOException, ProtocolException {
    String user = args[0];
    String password = args[1];
    CloseableHttpResponse ret;

    // unauthenticated call
    ret =
        sendRequest(
            URI.create("https://gateway-mg-eu.soimt.com/api.app/v1/user/country/list"),
            "GET",
            "",
            ContentType.APPLICATION_JSON,
            "",
            "");
    System.out.println(EntityUtils.toString(ret.getEntity()));

    // login
    byte[] deviceId = new byte[64];
    new Random().nextBytes(deviceId);
    ret =
        sendRequest(
            URI.create("https://gateway-mg-eu.soimt.com/api.app/v1/oauth/token"),
            "POST",
            "grant_type=password&username="
                + user
                + "&password="
                + HashUtils.sha1(password)
                + "&scope=all&deviceId="
                + HexUtils.bytesToHex(deviceId)
                + "###com.saicmotor.europecar&deviceType=0&loginType=2&language=DE",
            ContentType.APPLICATION_FORM_URLENCODED,
            "",
            "");
    System.out.println(EntityUtils.toString(ret.getEntity()));

    Map<String, Object> map =
        new GensonBuilder().create().deserialize(EntityUtils.toString(ret.getEntity()), Map.class);

    String token = ((Map<String, String>) map.get("data")).get("access_token");
    System.out.println(token);

    // get vehicles
    ret =
        sendRequest(
            URI.create("https://gateway-mg-eu.soimt.com/api.app/v1/vehicle/list"),
            "GET",
            "",
            ContentType.APPLICATION_JSON,
            token,
            "");
    System.out.println(EntityUtils.toString(ret.getEntity()));

    map =
        new GensonBuilder().create().deserialize(EntityUtils.toString(ret.getEntity()), Map.class);

    String vin =
        (((Map<String, List<Map<String, String>>>) map.get("data")).get("vinList"))
            .get(0)
            .get("vin");

    // get status of 1st vehicle
    ret =
        sendRequest(
            URI.create(
                "https://gateway-mg-eu.soimt.com/api.app/v1/vehicle/status?vin="
                    + HashUtils.sha256(vin)
                    + "&vehStatusReqType=2"),
            "GET",
            "",
            ContentType.APPLICATION_JSON,
            token,
            "");
    System.out.println(EntityUtils.toString(ret.getEntity()));

    ret =
        sendRequest(
            URI.create(
                "https://gateway-mg-eu.soimt.com/api.app/v1/vehicle/status?vin="
                    + HashUtils.sha256(vin)
                    + "&vehStatusReqType=2"),
            "GET",
            "",
            ContentType.APPLICATION_JSON,
            token,
            ret.getHeader("event-id").getValue());
    System.out.println(EntityUtils.toString(ret.getEntity()));

    // get charging status of 1st vehicle
    ret =
        sendRequest(
            URI.create(
                "https://gateway-mg-eu.soimt.com/api.app/v1/vehicle/charging/mgmtData?vin="
                    + HashUtils.sha256(vin)),
            "GET",
            "",
            ContentType.APPLICATION_JSON,
            token,
            "");
    System.out.println(EntityUtils.toString(ret.getEntity()));

    ret =
        sendRequest(
            URI.create(
                "https://gateway-mg-eu.soimt.com/api.app/v1/vehicle/charging/mgmtData?vin="
                    + HashUtils.sha256(vin)),
            "GET",
            "",
            ContentType.APPLICATION_JSON,
            token,
            ret.getHeader("event-id").getValue());
    System.out.println(EntityUtils.toString(ret.getEntity()));
  }

  public static CloseableHttpResponse sendRequest(
      URI endpoint,
      String httpMethod,
      String request,
      ContentType contentType,
      String token,
      String eventId)
      throws IOException {
    try (CloseableHttpClient httpclient =
        HttpClients.custom()
            .addResponseInterceptorFirst(
                (httpResponse, entityDetails, httpContext) -> {
                  if (httpResponse.getHeader("app-content-encrypted") != null
                      && httpResponse.getHeader("app-content-encrypted").getValue().equals("1")) {
                    String body =
                        EntityUtils.toString(((HttpEntityContainer) httpResponse).getEntity());
                    body =
                        EncryptionUtils.decryptResponse(
                            httpResponse.getHeader("app-send-date").getValue(),
                            httpResponse.getHeader("original-content-type").getValue(),
                            body);

                    ((HttpEntityContainer) httpResponse).setEntity(new StringEntity(body));
                  }
                })
            .build()) {
      HttpUriRequestBase httppost =
          httpMethod.equals("GET") ? new HttpGet(endpoint) : new HttpPost(endpoint);

      long appSendDate = System.currentTimeMillis();

      if (!StringUtils.isEmpty(request)) {
        request =
            EncryptionUtils.encryptRequest(
                endpoint.toString(),
                appSendDate,
                APIConfig.TENANT_ID,
                token,
                request,
                contentType.getMimeType());
        httppost.setEntity(new StringEntity(request, contentType));
      }

      httppost.setHeader("tenant-id", APIConfig.TENANT_ID);
      httppost.setHeader("user-type", APIConfig.USER_TYPE);
      httppost.setHeader("app-send-date", appSendDate);
      httppost.setHeader("app-content-encrypted", APIConfig.CONTENT_ENCRYPTED);
      httppost.setHeader("Authorization", APIConfig.PARAM_AUTHENTICATION);
      if (!StringUtils.isEmpty(token)) {
        httppost.setHeader("blade-auth", token);
      }
      if (!StringUtils.isEmpty(eventId)) {
        httppost.setHeader("event-id", eventId);
      }
      String replace =
          !StringUtils.isEmpty(endpoint.toString())
              ? endpoint.toString().replace(EncryptionUtils.BASE_URL_P, "/")
              : "";

      httppost.setHeader(
          "app-verification-string",
          EncryptionUtils.calculateRequestVerification(
              replace,
              appSendDate,
              APIConfig.TENANT_ID,
              contentType.getMimeType(),
              request,
              token));
      httppost.setHeader("original-content-type", contentType.getMimeType());

      return httpclient.execute(httppost);
    }
  }
}
