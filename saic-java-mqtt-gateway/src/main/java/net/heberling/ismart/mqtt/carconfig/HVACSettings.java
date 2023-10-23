package net.heberling.ismart.mqtt.carconfig;

public interface HVACSettings {

  Integer getMinAllowedTemp();

  Integer getMaxAllowedTemp();

  default int normalizeTemperature(Integer temp) {
    return Math.min(Math.max(temp, getMinAllowedTemp()), getMaxAllowedTemp());
  }

  byte mapTempToSaicApi(Integer temp);
}
