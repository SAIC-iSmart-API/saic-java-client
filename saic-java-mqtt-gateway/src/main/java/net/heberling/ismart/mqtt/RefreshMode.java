package net.heberling.ismart.mqtt;

import java.util.Arrays;
import java.util.Optional;

public enum RefreshMode {
  FORCE("force"),
  OFF("off"),
  PERIODIC("periodic");

  private String refreshMode;

  private RefreshMode(String refreshMode) {
    this.refreshMode = refreshMode;
  }

  public String getStringValue() {
    return refreshMode;
  }

  public static Optional<RefreshMode> get(String refreshMode) {
    return Arrays.stream(RefreshMode.values())
        .filter(value -> value.getStringValue().equals(refreshMode))
        .findFirst();
  }
}
