# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- API
  - Support for the new SAIC REST API ([#147](https://github.com/SAIC-iSmart-API/saic-java-client/pull/147))
- MQTT
  - support changing remote A/C temperature via setting `climate/remoteTemperature`

### Dependencies
- Bump `me.qoomon:maven-git-versioning-extension` from 9.6.5 to 9.8.1 ([#81](https://github.com/SAIC-iSmart-API/saic-java-client/pull/81), [#108](https://github.com/SAIC-iSmart-API/saic-java-client/pull/108), [#130](https://github.com/SAIC-iSmart-API/saic-java-client/pull/130))
- Bump `com.google.cloud.tools:jib-maven-plugin` from 3.3.2 to 3.4.4 ([#80](https://github.com/SAIC-iSmart-API/saic-java-client/pull/80), [#135](https://github.com/SAIC-iSmart-API/saic-java-client/pull/135), [#151](https://github.com/SAIC-iSmart-API/saic-java-client/pull/151))
- Bump `com.fasterxml.jackson.dataformat:jackson-dataformat-toml` from 2.15.2 to 2.18.2 ([#79](https://github.com/SAIC-iSmart-API/saic-java-client/pull/79), [#91](https://github.com/SAIC-iSmart-API/saic-java-client/pull/91), [#155](https://github.com/SAIC-iSmart-API/saic-java-client/pull/155))
- Bump `org.apache.maven.plugins:maven-shade-plugin` from 3.5.0 to 3.6.0 ([#77](https://github.com/SAIC-iSmart-API/saic-java-client/pull/77), [#128](https://github.com/SAIC-iSmart-API/saic-java-client/pull/128))
- Bump `com.sun.xml.bind:jaxb-impl` from 2.3.8 to 2.3.9 ([#78](https://github.com/SAIC-iSmart-API/saic-java-client/pull/78))
- Bump `org.apache.maven.plugins:maven-failsafe-plugin` from 3.1.2 to 3.5.2 ([#84](https://github.com/SAIC-iSmart-API/saic-java-client/pull/84), [#90](https://github.com/SAIC-iSmart-API/saic-java-client/pull/90), [#121](https://github.com/SAIC-iSmart-API/saic-java-client/pull/121), [#133](https://github.com/SAIC-iSmart-API/saic-java-client/pull/133), [#149](https://github.com/SAIC-iSmart-API/saic-java-client/pull/149))
- Bump `com.diffplug.spotless:spotless-maven-plugin` from 2.38.0 to 2.43.0 ([#83](https://github.com/SAIC-iSmart-API/saic-java-client/pull/83), [#103](https://github.com/SAIC-iSmart-API/saic-java-client/pull/103), [#112](https://github.com/SAIC-iSmart-API/saic-java-client/pull/112))
- Bump `org.graalvm.sdk:graal-sdk` from 23.0.1 to 24.0.2 ([#82](https://github.com/SAIC-iSmart-API/saic-java-client/pull/82), [#125](https://github.com/SAIC-iSmart-API/saic-java-client/pull/125))
- Bump `ASzc/change-string-case-action` from 5 to 6 ([#85](https://github.com/SAIC-iSmart-API/saic-java-client/pull/85))
- Bump `org.mockito:mockito-core` from 5.6.0 to 5.14.2 ([#86](https://github.com/SAIC-iSmart-API/saic-java-client/pull/86), [#101](https://github.com/SAIC-iSmart-API/saic-java-client/pull/101), [#148](https://github.com/SAIC-iSmart-API/saic-java-client/pull/148))
- Bump `org.mockito:mockito-junit-jupiter` from 5.6.0 to 5.12.0 ([#87](https://github.com/SAIC-iSmart-API/saic-java-client/pull/87), [#100](https://github.com/SAIC-iSmart-API/saic-java-client/pull/100), [#122](https://github.com/SAIC-iSmart-API/saic-java-client/pull/122))
- Bump `org.junit.jupiter:junit-jupiter` from 5.10.0 to 5.11.3 ([#88](https://github.com/SAIC-iSmart-API/saic-java-client/pull/88), [#157](https://github.com/SAIC-iSmart-API/saic-java-client/pull/157))
- Bump `org.apache.maven.plugins:maven-javadoc-plugin` from 3.6.0 to 3.11.1 ([#89](https://github.com/SAIC-iSmart-API/saic-java-client/pull/89), [#102](https://github.com/SAIC-iSmart-API/saic-java-client/pull/102), [#152](https://github.com/SAIC-iSmart-API/saic-java-client/pull/152))
- Bump `org.testcontainers:testcontainers-bom` from 1.19.1 to 1.20.4 ([#92](https://github.com/SAIC-iSmart-API/saic-java-client/pull/92), [#95](https://github.com/SAIC-iSmart-API/saic-java-client/pull/95), [#134](https://github.com/SAIC-iSmart-API/saic-java-client/pull/134), [#156](https://github.com/SAIC-iSmart-API/saic-java-client/pull/156))
- Bump `org.codehaus.mojo:exec-maven-plugin` from 3.1.0 to 3.5.0 ([#93](https://github.com/SAIC-iSmart-API/saic-java-client/pull/93), [#143](https://github.com/SAIC-iSmart-API/saic-java-client/pull/143))
- Bump `org.apache.httpcomponents.client5:httpclient5` from 5.2.1 to 5.4.1 ([#96](https://github.com/SAIC-iSmart-API/saic-java-client/pull/96), [#104](https://github.com/SAIC-iSmart-API/saic-java-client/pull/104), [#144](https://github.com/SAIC-iSmart-API/saic-java-client/pull/144))
- Bump `org.codehaus.mojo:build-helper-maven-plugin` from 3.4.0 to 3.6.0 ([#97](https://github.com/SAIC-iSmart-API/saic-java-client/pull/97), [#136](https://github.com/SAIC-iSmart-API/saic-java-client/pull/136))
- Bump `s4u/maven-settings-action` from 2.8.0 to 3.1.0 ([#107](https://github.com/SAIC-iSmart-API/saic-java-client/pull/107), [#145](https://github.com/SAIC-iSmart-API/saic-java-client/pull/145))
- Bump `org.graalvm.buildtools:native-maven-plugin` from 0.9.28 to 0.10.3 ([#126](https://github.com/SAIC-iSmart-API/saic-java-client/pull/126), [#140](https://github.com/SAIC-iSmart-API/saic-java-client/pull/140))
- Bump `org.apache.maven.plugins:maven-jar-plugin` from 3.3.0 to 3.4.2 ([#124](https://github.com/SAIC-iSmart-API/saic-java-client/pull/124))
- Bump `org.apache.maven.plugins:maven-source-plugin` from 3.3.0 to 3.3.1 ([#131](https://github.com/SAIC-iSmart-API/saic-java-client/pull/131))
- Bump `version.picocli` from 4.7.5 to 4.7.6 ([#132](https://github.com/SAIC-iSmart-API/saic-java-client/pull/132))
- Bump `jakarta.xml.bind:jakarta.xml.bind-api` from 4.0.1 to 4.0.2 ([#137](https://github.com/SAIC-iSmart-API/saic-java-client/pull/137))
- Bump `org.vafer:jdeb` from 1.10 to 1.12 ([#146](https://github.com/SAIC-iSmart-API/saic-java-client/pull/146))
- Bump `org.apache.maven.plugins:maven-deploy-plugin` from 3.1.1 to 3.1.3 ([#141](https://github.com/SAIC-iSmart-API/saic-java-client/pull/141))
- Bump `org.awaitility:awaitility` from 4.2.0 to 4.2.2 ([#153](https://github.com/SAIC-iSmart-API/saic-java-client/pull/153))

## [0.3.0] - 2023-10-21
### Added
- API
  - ASN.1 Types for `OTA_ChrgCtrlReq` and `OTA_ChrgCtrlStsResp`
- MQTT
  - support starting/stopping charging via setting `drivetrain/charging`
  - added topic `drivetrain/remainingChargingTime`

### Changed
- MQTT
  - **Breaking** The default refresh rate while the car is active has been changed to 30 seconds
  - **Breaking** The default refresh rate while the car is inactive has been changed to 24 hours
  - **Breaking** encode dates as unquoted ISO 8601 strings with offset and without timezone
  - support configuring `refresh/mode`, `refresh/period/active`, `refresh/period/inActive` and `refresh/period/inActiveGrace` via MQTT
  - Handle fallback for SOC when charge status update fails
  - ensure that a changed systemd configuration is picked up
  - support blowingOnly mode for `remoteClimateState`
- API
  - Handle fallback for SOC when charge status update fails

### Fixed
- MQTT
  - keep message fetch thread alive after connection failures
  - Make sure car state is updated after successful command
  - never publish `force` to the `refresh/mode` to prevent never ending polling
  - prevent setting previous refresh mode to the same value as the current #55
  - set force refresh only for real car commands

### Dependencies
- Bump `version.picocli` from 4.7.3 to 4.7.5 (#29, [#73](https://github.com/SAIC-iSmart-API/saic-java-client/pull/73))
- Bump `maven-failsafe-plugin` from 3.1.0 to 3.1.2 (#30)
- Bump `graal-sdk` from 22.3.2 to 23.0.1 (#33, #53)
- Bump `native-maven-plugin` from 0.9.22 to 0.9.23 (#40)
- Bump `maven-shade-plugin` from 3.4.1 to 3.5.0 (#38)
- Bump `mockito-junit-jupiter` from 5.3.1 to 5.4.0 (#39)
- Bump `mockito-core` from 5.3.1 to 5.4.0 (#37)
- Bump `spotless-maven-plugin` from 2.37.0 to 2.38.0 (#52)
- Bump `org.junit.jupiter:junit-jupiter` from 5.9.3 to 5.10.0 ([#57](https://github.com/SAIC-iSmart-API/saic-java-client/pull/57))
- Bump `org.mockito:mockito-core` from 5.4.0 to 5.6.0 ([#71](https://github.com/SAIC-iSmart-API/saic-java-client/pull/71))
- Bump `org.mockito:mockito-junit-jupiter` from 5.4.0 to 5.6.0 ([#72](https://github.com/SAIC-iSmart-API/saic-java-client/pull/72))
- Bump `stefanzweifel/git-auto-commit-action` from 4 to 5 ([#69](https://github.com/SAIC-iSmart-API/saic-java-client/pull/69))
- Bump `org.apache.maven.plugins:maven-javadoc-plugin` from 3.5.0 to 3.6.0 ([#74](https://github.com/SAIC-iSmart-API/saic-java-client/pull/74))
- Bump `jakarta.xml.bind:jakarta.xml.bind-api` from 4.0.0 to 4.0.1 ([#75](https://github.com/SAIC-iSmart-API/saic-java-client/pull/75))
- Bump `org.testcontainers:testcontainers-bom` from 1.18.3 to 1.19.1 ([#68](https://github.com/SAIC-iSmart-API/saic-java-client/pull/68))
- Bump `org.graalvm.buildtools:native-maven-plugin` from 0.9.23 to 0.9.28 ([#76](https://github.com/SAIC-iSmart-API/saic-java-client/pull/76))
- Bump `actions/checkout` from 3 to 4 ([#64](https://github.com/SAIC-iSmart-API/saic-java-client/pull/64))

## [0.2.1] - 2023-06-03
### Fixed
- MQTT
  - calculate correct tyre pressure

### Dependencies
- Bump `testcontainers-bom` from 1.18.1 to 1.18.3 (#27)
- Bump `maven-source-plugin` from 3.2.1 to 3.3.0 (#23)
- Bump `spotless-maven-plugin` from 2.36.0 to 2.37.0 (#24)
- Bump `jackson-dataformat-toml` from 2.15.1 to 2.15.2 (#25)

## [0.2.0] - 2023-04-02
### Changed
- extracted saic-ismart-client

### Fixed
- MQTT
  - log ABRP errors, don't fail the whole thread
  - keep last `drivetrain/hvBatteryActive` state until it's updated from the API
  - allow setting the `drivetrain/hvBatteryActive/set` state to force updates
  - forbid retained set messages
  - added topics `refresh/lastVehicleState` and `refresh/lastChargeState`

## [0.1.0] - 2023-03-29
### Added
- Initial support for SAIC API
- Initial HTTP Gateway
- Initial MQTT Gateway
  - Support for A Better Routeplaner Telemetry update
  - automatically register for all alarm types
  - create Docker image
  - create Debian package
  - support `climate/remoteClimateState/set` with `off`, `on` and `front`
  - support `doors/locked/set` with `true` and `false`

[Unreleased]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.2.1...v0.3.0
[0.2.1]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/SAIC-iSmart-API/saic-java-client/releases/tag/v0.1.0