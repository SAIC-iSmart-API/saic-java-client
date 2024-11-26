package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public class MessageList extends JsonResponseMessage {

  String data;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
