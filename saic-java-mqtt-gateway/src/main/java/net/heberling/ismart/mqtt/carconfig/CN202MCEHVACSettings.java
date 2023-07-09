package net.heberling.ismart.mqtt.carconfig;

// India "HECTOR MCE";
public class CN202MCEHVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 17;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 33;
  }

  @Override
  public int normalizeTemperature(Integer temp) {
    return Math.min(Math.max(temp, getMinAllowedTemp()), getMaxAllowedTemp());
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) (normalizeTemperature(temp) - 14);
  }
}
