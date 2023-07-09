package net.heberling.ismart.mqtt.carconfig;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HVACSettingsFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(HVACSettingsFactory.class);

  public static HVACSettings getHVACSettingsFrom(URI saicUri, String series) {
    if (saicUri.getHost().contains("tap-eu")) {
      if (series.startsWith("MG4")) {
        return new EH32HVACSettings();
      } else {
        return new DefaultHVACSettings();
      }
    } else if (saicUri.getHost().contains("tap-au")) {
      return new DefaultHVACSettings();

    } else if (saicUri.getHost().contains("tap.mgindia")) {
      if (series.startsWith("CN202SR")) {
        return new CN202HVACSettings();
      } else if (series.startsWith("HECTOR MCE")) {
        return new CN202MCEHVACSettings();
      } else if (series.startsWith("GLOSTER")) {
        return new D90HVACSettings();
      } else if (series.startsWith("NEW ZS")) {
        return new ZS11MCEHVACSettings();
      } else if (series.startsWith("ZS EV MCE")) {
        return new ZSEVMCEHVACSettings();
      } else {
        return new ZS11EHVACSettings();
      }
    } else {
      LOGGER.warn("Unknown vehicle: '{}', using default value...", series);
      return new DefaultHVACSettings();
    }
  }
}
