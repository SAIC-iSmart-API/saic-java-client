package net.heberling.ismart.mqtt.carconfig;

// MG4 Europe
public class EH32HVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 17;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 33;
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) (normalizeTemperature(temp) - 14);
  }
}
