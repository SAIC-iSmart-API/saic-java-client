package net.heberling.ismart.mqtt.carconfig;

public class DefaultHVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 16;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 28;
  }

  @Override
  public Integer normalizeTemperature(Integer temp) {
    return Math.min(Math.max(temp, getMinAllowedTemp()), getMaxAllowedTemp());
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) (normalizeTemperature(temp) - 14);
  }
}
