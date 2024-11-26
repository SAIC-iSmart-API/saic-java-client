package net.heberling.ismart.java.rest.exceptions;

import net.heberling.ismart.java.rest.api.v1.JsonResponseMessage;

/**
 * @author Doug Culnane - Initial contribution
 */
public class VehicleStatusAPIException extends Exception {

  public VehicleStatusAPIException(JsonResponseMessage response) {
    super("[" + response.getCode() + "] " + response.getMessage());
  }
}
