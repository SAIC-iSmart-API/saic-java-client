package net.heberling.ismart.mqtt.carconfig;

// ZS EV, MG5, Marvel Europe, Default Australia
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
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) (normalizeTemperature(temp) - 14);
  }
}
