package net.heberling.ismart.mqtt.carconfig;

public interface HVACSettings {

    Integer getMinAllowedTemp();

    Integer getMaxAllowedTemp();

    Integer normalizeTemperature(Integer temp);

    byte mapTempToSaicApi(Integer temp);
}
