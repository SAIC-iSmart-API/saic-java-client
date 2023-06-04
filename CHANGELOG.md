# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- MQTT
  - **Breaking** The default refresh rate while the car is active has been changed to 30 seconds
  - **Breaking** The default refresh rate while the car is inactive has been changed to 24 hours
  - support configuring `refresh/mode`, `refresh/period/active`, `refresh/period/inActive` and `refresh/period/inActiveGrace` via MQTT

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

[Unreleased]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.2.1...HEAD
[0.2.1]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/SAIC-iSmart-API/saic-java-client/releases/tag/v0.1.0