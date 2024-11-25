package net.heberling.ismart.java.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.heberling.ismart.java.rest.api.v1.OauthToken;
import net.heberling.ismart.java.rest.api.v1.VehicleList;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Culnane
 */
public class TestSaicApiClient {

  private final Logger logger = LoggerFactory.getLogger(TestSaicApiClient.class);

  @Test
  public void testClient() throws Exception {

    // Set the environment variables to run the test against the API
    String username = System.getenv("SAIC_USERNAME");
    String password = System.getenv("SAIC_PASSWORD");
    if (username == null || password == null) {
      logger.warn(
          "To activate SaicApiClient tests set the SAIC_USERNAME and SAIC_PASSWORD environment"
              + " variables.");
      return;
    }

    HttpClient httpClient =
        new HttpClient(new HttpClientTransportOverHTTP(), new SslContextFactory(true));
    httpClient.start();

    SaicApiClient client = new SaicApiClient(httpClient);
    OauthToken oauthToken = client.getOauthToken(username, password, "EN");

    String token = oauthToken.getData().getAccess_token();
    VehicleList vehicleList = client.getVehicleList(token);
    String vin = vehicleList.getData().getVinList()[0].getVin();

    assertTrue(client.getVehicleStatus(token, vin).isSuccess());
    assertTrue(client.getVehicleStatisticsBasicInfo(token, vin).isSuccess());
    assertTrue(client.getVehicleChargingMgmtData(token, vin).isSuccess());
    assertTrue(client.getMessageNotificationList(token).isSuccess());

    // Not much useful info
    // assertTrue(client.getVehicleGeoFence(token, vin).isSuccess());
    // assertTrue(client.getVehicleStatisticsBasicInfo(token, vin).isSuccess());

    // Failing
    // assertTrue(client.getVehicleInfo(token, vin).isSuccess());
    // assertTrue(client.getVehicleStatusClassified(token, vin).isSuccess());
    // assertTrue(client.getMessageList(token, vin).isSuccess());
    // assertTrue(client.getVehicleCcInfo(token, vin).isSuccess());
    // assertTrue(client.getVehicleLocation(token, vin).isSuccess());
  }
}
