package net.heberling.ismart.mqtt.carconfig;

// India "ZS EV MCE";
public class ZSEVMCEHVACSettings implements HVACSettings {
  @Override
  public Integer getMinAllowedTemp() {
    return 16;
  }

  @Override
  public Integer getMaxAllowedTemp() {
    return 29;
  }

  @Override
  public byte mapTempToSaicApi(Integer temp) {
    return (byte) normalizeTemperature(temp);
  }
}
