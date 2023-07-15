package net.heberling.ismart.mqtt.carconfig;

public interface HVACSettings {

  default Integer getMinAllowedTemp() {
    return 16;
  }

  default Integer getMaxAllowedTemp() {
    return 28;
  }

  default Integer normalizeTemperature(Integer temp) {
    return Math.min(Math.max(temp, getMinAllowedTemp()), getMaxAllowedTemp());
  }

  default byte mapTempToSaicApi(Integer temp) {
    return (byte) (normalizeTemperature(temp) - 14);
  }

  default String toRemoteClimate(Integer remoteClimateStatus) {
    switch (remoteClimateStatus) {
      case 0:
        return "off";
      case 1:
        return "blowingOnly";
      case 2:
        return "on";
      case 5:
        return "front";
      default:
        return "unknown (" + remoteClimateStatus + ")";
    }
  }
}
