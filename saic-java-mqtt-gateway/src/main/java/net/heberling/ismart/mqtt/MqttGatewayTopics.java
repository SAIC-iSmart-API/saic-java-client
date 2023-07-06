package net.heberling.ismart.mqtt;

public class MqttGatewayTopics {
  public static final String CLIMATE = "climate";
  public static final String CLIMATE_BACK_WINDOW_HEAT = CLIMATE + "/backWindowHeat";
  public static final String CLIMATE_EXTERIOR_TEMPERATURE = CLIMATE + "/exteriorTemperature";
  public static final String CLIMATE_INTERIOR_TEMPERATURE = CLIMATE + "/interiorTemperature";
  public static final String CLIMATE_REMOTE_CLIMATE_STATE = CLIMATE + "/remoteClimateState";
  public static final String DOORS = "doors";
  public static final String DOORS_BONNET = DOORS + "/bonnet";
  public static final String DOORS_BOOT = DOORS + "/boot";
  public static final String DOORS_DRIVER = DOORS + "/driver";
  public static final String DOORS_LOCKED = DOORS + "/locked";
  public static final String DOORS_PASSENGER = DOORS + "/passenger";
  public static final String DOORS_REAR_LEFT = DOORS + "/rearLeft";
  public static final String DOORS_REAR_RIGHT = DOORS + "/rearRight";
  public static final String DRIVETRAIN = "drivetrain";
  public static final String DRIVETRAIN_AUXILIARY_BATTERY_VOLTAGE =
      DRIVETRAIN + "/auxiliaryBatteryVoltage";
  public static final String DRIVETRAIN_CHARGER_CONNECTED = DRIVETRAIN + "/chargerConnected";
  public static final String DRIVETRAIN_CHARGING = DRIVETRAIN + "/charging";
  public static final String DRIVETRAIN_CHARGING_TYPE = DRIVETRAIN + "/chargingType";
  public static final String DRIVETRAIN_CURRENT = DRIVETRAIN + "/current";
  public static final String DRIVETRAIN_HV_BATTERY_ACTIVE = DRIVETRAIN + "/hvBatteryActive";
  public static final String DRIVETRAIN_MILEAGE = DRIVETRAIN + "/mileage";
  public static final String DRIVETRAIN_POWER = DRIVETRAIN + "/power";
  public static final String DRIVETRAIN_RANGE = DRIVETRAIN + "/range";
  public static final String DRIVETRAIN_RUNNING = DRIVETRAIN + "/running";
  public static final String DRIVETRAIN_SOC = DRIVETRAIN + "/soc";
  public static final String DRIVETRAIN_VOLTAGE = DRIVETRAIN + "/voltage";
  public static final String DRIVETRAIN_REMAINING_CHARGING_TIME =
      DRIVETRAIN + "/remainingChargingTime";
  public static final String INFO = "info";
  public static final String INFO_CONFIGURATION = INFO + "/configuration";
  public static final String INFO_LAST_MESSAGE = INFO + "/lastMessage";
  public static final String INTERNAL = "_internal";
  public static final String INTERNAL_ABRP = INTERNAL + "/abrp";
  public static final String INTERNAL_CONFIGURATION_RAW = INTERNAL + "/configuration/raw";
  public static final String LOCATION = "location";
  public static final String LOCATION_HEADING = LOCATION + "/heading";
  public static final String LOCATION_POSITION = LOCATION + "/position";
  public static final String LOCATION_SPEED = LOCATION + "/speed";
  public static final String REFRESH = "refresh";
  public static final String REFRESH_LAST_ACTIVITY = REFRESH + "/lastActivity";
  public static final String REFRESH_LAST_CHARGE_STATE = REFRESH + "/lastChargeState";
  public static final String REFRESH_LAST_VEHICLE_STATE = REFRESH + "/lastVehicleState";
  public static final String REFRESH_MODE = REFRESH + "/mode";
  public static final String REFRESH_PERIOD = REFRESH + "/period";
  public static final String REFRESH_PERIOD_ACTIVE = REFRESH_PERIOD + "/active";
  public static final String REFRESH_PERIOD_INACTIVE = REFRESH_PERIOD + "/inActive";
  public static final String REFRESH_PERIOD_INACTIVE_GRACE = REFRESH_PERIOD + "/inActiveGrace";
  public static final String TYRES = "tyres";
  public static final String TYRES_FRONT_LEFT_PRESSURE = TYRES + "/frontLeftPressure";
  public static final String TYRES_FRONT_RIGHT_PRESSURE = TYRES + "/frontRightPressure";
  public static final String TYRES_REAR_LEFT_PRESSURE = TYRES + "/rearLeftPressure";
  public static final String TYRES_REAR_RIGHT_PRESSURE = TYRES + "/rearRightPressure";
  public static final String VEHICLES = "vehicles";
}
