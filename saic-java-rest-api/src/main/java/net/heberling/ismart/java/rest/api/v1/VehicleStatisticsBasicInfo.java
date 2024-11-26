package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public class VehicleStatisticsBasicInfo extends JsonResponseMessage {

  VehicleStatisticsBasicInfoData data;

  public VehicleStatisticsBasicInfo() {}

  public VehicleStatisticsBasicInfoData getData() {
    return data;
  }

  public void setData(VehicleStatisticsBasicInfoData data) {
    this.data = data;
  }

  public class VehicleStatisticsBasicInfoData {

    String vin;
    Long initialDate;

    public VehicleStatisticsBasicInfoData() {}

    public String getVin() {
      return vin;
    }

    public void setVin(String vin) {
      this.vin = vin;
    }

    public Long getInitialDate() {
      return initialDate;
    }

    public void setInitialDate(Long initialDate) {
      this.initialDate = initialDate;
    }
  }
}
