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
  public int normalizeTemperature(Integer temp) {
    return Math.min(Math.max(temp, getMinAllowedTemp()), getMaxAllowedTemp());
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) normalizeTemperature(temp);
  }
}
