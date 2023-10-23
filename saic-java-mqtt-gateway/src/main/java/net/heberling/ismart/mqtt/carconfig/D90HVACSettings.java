package net.heberling.ismart.mqtt.carconfig;

// India "GLOSTER";
public class D90HVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 17;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 31;
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) normalizeTemperature(temp);
  }
}
