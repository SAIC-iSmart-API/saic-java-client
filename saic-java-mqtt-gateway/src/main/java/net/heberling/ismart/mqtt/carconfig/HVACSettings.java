package net.heberling.ismart.mqtt.carconfig;

public interface HVACSettings {

    Integer getMinAllowedTemp();

    Integer getMaxAllowedTemp();

    boolean isTempWithinRange(Integer temp);

    byte mapTempToSaicApi(Integer temp);
}
