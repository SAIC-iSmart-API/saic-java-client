package net.heberling.ismart.mqtt.carconfig;

// India "NEW ZS";
public class ZS11MCEHVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 15;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 29;
  }

  @Override
  public int normalizeTemperature(Integer temp) {
    return Math.min(Math.max(temp, getMinAllowedTemp()), getMaxAllowedTemp());
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) normalizeTemperature(temp);
  }
}
