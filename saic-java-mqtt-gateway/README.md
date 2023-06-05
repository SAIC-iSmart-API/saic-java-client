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

### 2. Debian Package

Download the Debian package `saic-ismart-mqtt-gateway-VERSION.deb` from https://github.com/SAIC-iSmart-API/saic-java-client/releases/latest.

Install the MQTT gateway with apt:

```bash
apt-get install saic-ismart-mqtt-gateway-VERSION.deb
```

Configure the MQTT gateway in `/etc/saic-mqtt-gateway.toml`:

```toml
[mqtt]
uri = "tcp://localhost:1883"
username = "mqtt_user"
password = "mqtt_pass"

[saic]
uri = "https://tap-eu.soimt.com"
username = "ismart_user"
password = "ismart_password"

[abrp]
api-key = "8cfc314b-03cd-4efe-ab7d-4431cd8f2e2d"

[[abrp.token]]
"vin1" = "token1"

[[abrp.token]]
"vin2" = "token2"
```

Enable and start the gateway:

```bash
systemctl enable saic-mqtt-gateway
systemctl start saic-mqtt-gateway
```

You can check the log output with

```bash
journalctl -u saic-mqtt-gateway -f
```

### 3. Run from command line

Download the full-jar package `saic-ismart-mqtt-gateway-VERSION-full.jar` from https://github.com/SAIC-iSmart-API/saic-java-client/releases/latest.

You can run it with the following command:
```bash
java -jar saic-ismart-mqtt-gateway-VERSION-full.jar \
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
    `saic/{saic-user}/vehicles/{vin}`

* `drivetrain/hvBatteryActive` {true|false}
* `climate/remoteClimateState` {true|false}
* `doors/locked` {true|false}
* `refresh/mode` {periodic|off|force}
* `refresh/period/active` {seconds} -> Interval in seconds to poll the car state when hvBattery is active (30s default)
* `refresh/period/inActive` {seconds} -> Interval in seconds to poll the car state when hvBattery is inActive (86400s default)
* `refresh/period/inActiveGrace` {seconds} -> -> Interval in seconds handle car state as active after hvBattery was disconnected (600s default)

To set these values, just post a message to the corresponding topic plus `/set` with the desired value. 

Further commands will be supported in the future.
