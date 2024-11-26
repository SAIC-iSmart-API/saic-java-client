package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public abstract class JsonResponseMessage {

  public static final Integer CODE_SUCCESS = 0;

  Integer code;
  String message;
  String eventId;

  abstract Object getData();

  public boolean hasData() {
    return getData() != null;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public JsonResponseMessage() {}

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "[" + code + "] " + message;
  }

  public boolean isSuccess() {
    return code == CODE_SUCCESS;
  }
}
