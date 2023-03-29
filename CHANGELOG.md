# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Fixed
- API
  - support messages with DispatcherBody data longer than 256 bytes

### Changed
- properly initialize Messages in the MessageCoder

### Added
- Initial support for SAIC API
- Initial MQTT Gateway
  - Support for A Better Routeplaner Telemetry update
- Initial HTTP Gateway
- Native MQTT Gateway Docker build
- MQTT
  - added --saic-uri configuration option
  - automatically register for all alarm types
  - create Docker image
  - create Debian package
  - support `climate/remoteClimateState/set` with `off`, `on` and `front`
  - support `doors/locked/set` with `true` and `false`