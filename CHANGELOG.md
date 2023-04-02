# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
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

[Unreleased]: https://github.com/SAIC-iSmart-API/saic-java-client/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/SAIC-iSmart-API/saic-java-client/releases/tag/v0.1.0
