package net.heberling.ismart.mqtt.carconfig;

public interface HVACSettings {

  Integer getMinAllowedTemp();

  Integer getMaxAllowedTemp();

  int normalizeTemperature(Integer temp);

  byte mapTempToSaicApi(Integer temp);
}
