package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public class VehicleList extends JsonResponseMessage {

  VinListData data;

  public VinListData getData() {
    return data;
  }

  public void setData(VinListData data) {
    this.data = data;
  }

  public class VinListData {
    public VinListData() {}

    VinListItem[] vinList;

    public VinListItem[] getVinList() {
      return vinList;
    }

    public void setVinList(VinListItem[] vinList) {
      this.vinList = vinList;
    }
  }

  public class VinListItem {
    Long bindTime;
    String colorName;
    String brandName;
    String modelName;
    String modelYear;
    String series;
    Boolean isActivate;
    Boolean isCurrentVehicle;
    Boolean isSubaccount;
    String vin;
    VehicleModelConfiguration[] vehicleModelConfiguration;

    public VinListItem() {}

    public Long getBindTime() {
      return bindTime;
    }

    public void setBindTime(Long bindTime) {
      this.bindTime = bindTime;
    }

    public String getColorName() {
      return colorName;
    }

    public void setColorName(String colorName) {
      this.colorName = colorName;
    }

    public String getBrandName() {
      return brandName;
    }

    public void setBrandName(String brandName) {
      this.brandName = brandName;
    }

    public String getModelName() {
      return modelName;
    }

    public void setModelName(String modelName) {
      this.modelName = modelName;
    }

    public String getModelYear() {
      return modelYear;
    }

    public void setModelYear(String modelYear) {
      this.modelYear = modelYear;
    }

    public String getSeries() {
      return series;
    }

    public void setSeries(String series) {
      this.series = series;
    }

    public Boolean getIsActivate() {
      return isActivate;
    }

    public void setIsActivate(Boolean isActivate) {
      this.isActivate = isActivate;
    }

    public Boolean getIsCurrentVehicle() {
      return isCurrentVehicle;
    }

    public void setIsCurrentVehicle(Boolean isCurrentVehicle) {
      this.isCurrentVehicle = isCurrentVehicle;
    }

    public Boolean getIsSubaccount() {
      return isSubaccount;
    }

    public void setIsSubaccount(Boolean isSubaccount) {
      this.isSubaccount = isSubaccount;
    }

    public String getVin() {
      return vin;
    }

    public void setVin(String vin) {
      this.vin = vin;
    }

    public VehicleModelConfiguration[] getVehicleModelConfiguration() {
      return vehicleModelConfiguration;
    }

    public void setVehicleModelConfiguration(
        VehicleModelConfiguration[] vehicleModelConfiguration) {
      this.vehicleModelConfiguration = vehicleModelConfiguration;
    }
  }

  public class VehicleModelConfiguration {
    String itemName;
    String itemCode;
    String itemValue;

    public String getItemName() {
      return itemName;
    }

    public void setItemName(String itemName) {
      this.itemName = itemName;
    }

    public String getItemCode() {
      return itemCode;
    }

    public void setItemCode(String itemCode) {
      this.itemCode = itemCode;
    }

    public String getItemValue() {
      return itemValue;
    }

    public void setItemValue(String itemValue) {
      this.itemValue = itemValue;
    }
  }
}
