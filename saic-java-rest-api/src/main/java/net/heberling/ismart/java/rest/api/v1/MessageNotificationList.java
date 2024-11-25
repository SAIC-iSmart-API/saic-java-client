package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public class MessageNotificationList extends JsonResponseMessage {

  MessageNotificationListData data;

  public MessageNotificationListData getData() {
    return data;
  }

  public void setData(MessageNotificationListData data) {
    this.data = data;
  }

  public class MessageNotificationListData {
    Long recordsNumber;
    Notification[] notifications;

    public Long getRecordsNumber() {
      return recordsNumber;
    }

    public void setRecordsNumber(Long recordsNumber) {
      this.recordsNumber = recordsNumber;
    }

    public Notification[] getNotifications() {
      return notifications;
    }

    public void setNotifications(Notification[] notifications) {
      this.notifications = notifications;
    }
  }

  public class Notification {
    Integer readStatus;
    String messageTime;
    String messageType;
    String sender;
    Long messageId;
    String vin;
    String title;
    String content;

    public Integer getReadStatus() {
      return readStatus;
    }

    public void setReadStatus(Integer readStatus) {
      this.readStatus = readStatus;
    }

    public String getMessageTime() {
      return messageTime;
    }

    public void setMessageTime(String messageTime) {
      this.messageTime = messageTime;
    }

    public String getMessageType() {
      return messageType;
    }

    public void setMessageType(String messageType) {
      this.messageType = messageType;
    }

    public String getSender() {
      return sender;
    }

    public void setSender(String sender) {
      this.sender = sender;
    }

    public Long getMessageId() {
      return messageId;
    }

    public void setMessageId(Long messageId) {
      this.messageId = messageId;
    }

    public String getTitle() {
      return title;
    }

    public String getVin() {
      return vin;
    }

    public void setVin(String vin) {
      this.vin = vin;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }
}
