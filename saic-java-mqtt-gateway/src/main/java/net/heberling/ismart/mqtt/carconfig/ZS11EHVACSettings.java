package net.heberling.ismart.mqtt.carconfig;

// Default India
public class ZS11EHVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 16;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 30;
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) normalizeTemperature(temp);
  }
}
