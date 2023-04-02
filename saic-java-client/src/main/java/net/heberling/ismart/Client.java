package net.heberling.ismart;

import java.io.IOException;
import java.net.URI;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class Client {
  public static String sendRequest(URI endpoint, String request) throws IOException {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httppost = new HttpPost(endpoint);
      // Request parameters and other properties.
      httppost.setEntity(new StringEntity(request, ContentType.TEXT_HTML));

      // Execute and get the response.
      // Create a custom response handler
      HttpClientResponseHandler<String> responseHandler =
          response -> {
            final int status = response.getCode();
            if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
              final HttpEntity entity = response.getEntity();
              try {
                return entity != null ? EntityUtils.toString(entity) : null;
              } catch (final ParseException ex) {
                throw new ClientProtocolException(ex);
              }
            } else {
              throw new ClientProtocolException("Unexpected response status: " + status);
            }
          };
      return httpclient.execute(httppost, responseHandler);
    }
  }
}
