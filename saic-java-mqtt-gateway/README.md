# SAIC-JAVA-MQTT-GATEWAY

This gateway allows to poll the car state and publish it to 
a MQTT broker. It is written in Java and uses the saic-java-api 
from the same project.


## Running

### 1. Docker-compose

The easiest way to run the gateway is to use the docker-compose file. 

```
version: "3.9"
services:
  ismart_mqtt:
    image: saicismartapi/saic-java-mqtt-gateway:latest
    environment:
      SAIC_USER: {your saic user}
      SAIC_PASSWORD: {your saic password}
      MQTT_URI: {your mqtt broker uri}
      MQTT_USER: {your mqtt broker user}
      MQTT_PASSWORD: {your mqtt broker password}
      ABRP_USER_TOKEN: {your abrp user token}
    restart: on-failure
```
The ABRP_USER_TOKEN is optional. If you provide it, the gateway will also publish the car state to the ABRP API.

For the token, add comma-sepparated entries for each vehicle like this: {VIN=token,VIN2=token2,...}

### 2. Run from command line

You can also run the gateway from the command line. For this, you need to build the jar file first.

From the parent project, run:
```
mvn package
```

If everything goes right, you will have a jar file (saic-ismart-mqtt-gateway-0.0.0-SNAPSHOT-full.jar) 
in the target folder. You can run it with the following command:
```
java -jar saic-ismart-mqtt-gateway-0.0.0-SNAPSHOT-full.jar \
    --saic-user={your saic user} \
    --saic-password={your saic password} \
    --mqtt-uri={your mqtt broker uri} \
    --mqtt-user={your mqtt broker user} \
    --mqtt-password={your mqtt broker password} \
    --abrp-user-token={your abrp user token}
```

## MQTT Broker
In case you need a MQTT Broker, you could use mosquitto from eclipse. https://mosquitto.org

> mosquitto is not required to run the gateway. You can use any MQTT broker you want.

If you plan on running mosquitto as a docker image, you could use this template
as a starting point: https://github.com/vvatelot/mosquitto-docker-compose

> Note: In case you use mosquitto, prefer tcp over ws protocol for the gateway.

## Commands

The gateway supports the following commands from mqtt topics:

Base topic is always:
    saic/{saic-user}/vehicles/{vin}

* drivetrain/hvBatteryActive {true|false}
* climate/remoteClimateState {true|false}
* doors/locked {true|false}
* refresh/mode {periodic|off|force}
* refresh/period/active {seconds} -> Interval in seconds to poll the car state when hvBattery is active (30s default)
* refresh/period/inActive {seconds} -> Interval in seconds to poll the car state when hvBattery is inActive (86400s default)
* refresh/period/inActiveGrace {seconds} -> -> Interval in seconds handle car state as active after hvBattery was disconnected (600s default)

To set these values, just post a message to the corresponding topic plus "/set" with the desired value. 

Further commands will be supported in the future.
