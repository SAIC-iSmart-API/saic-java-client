package net.heberling.ismart.java.rest.exceptions;

import net.heberling.ismart.java.rest.api.v1.JsonResponseMessage;

/**
 * @author Doug Culnane - Initial contribution
 */
public class ChargingStatusAPIException extends Exception {

  public ChargingStatusAPIException(JsonResponseMessage response) {
    super("[" + response.getCode() + "] " + response.getMessage());
  }
}
