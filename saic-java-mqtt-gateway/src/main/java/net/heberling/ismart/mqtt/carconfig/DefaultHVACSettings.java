package net.heberling.ismart.mqtt.carconfig;

public class DefaultHVACSettings implements HVACSettings {
    @Override
    public Integer getMinAllowedTemp() {
        return 16;
    }

    @Override
    public Integer getMaxAllowedTemp() {
        return 28;
    }

    @Override
    public boolean isTempWithinRange(Integer temp) {
        return temp >= getMinAllowedTemp() && temp <= getMaxAllowedTemp();
    }

    @Override
    public byte mapTempToSaicApi(Integer temp) {
        if(!isTempWithinRange(temp)) {
            throw new IllegalArgumentException("Temperature must be between " + getMinAllowedTemp() + " and " + getMaxAllowedTemp());
        }
        return (byte) (temp - 14);
    }
}
