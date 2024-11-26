package net.heberling.ismart.java.rest.api.v1;

/**
 * Returns null...?
 *
 * @author Doug Culnane
 */
public class VehicleCcInfo extends JsonResponseMessage {

  String data;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
