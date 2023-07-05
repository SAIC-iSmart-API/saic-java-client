
# SAIC Java API

Implementation of the SAIC API in Java.


## Badges

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)

![Maven Central](https://img.shields.io/maven-central/v/io.github.saic-ismart-api/saic-ismart-api-parent)

![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/SAIC-iSmart-API/saic-java-client/maven.yml)

## Documentation

[SAIC API Documentation](documentation)


## Usage

[MQTT Gateway](saic-java-mqtt-gateway)


## Build locally

Clone the project

```bash
git clone git@github.com:SAIC-iSmart-API/saic-java-client.git
```

Go to the project directory

```bash
cd saic-java-client
```

Checkout submodules
```bash
git submodule init
git submodule update
```

Build and test

```bash
./mvnw clean verify
```

Skip docker image creation

```bash
./mvnw clean verify -Djib.skip
```
## Contributing

Contributions are always welcome!

