# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project follows Semantic Versioning.

## [1.0.1] - 2026-05-11

### Fixed

- Fixed release build failure on R8 minification by adding missing `javax.annotation` classes dependency (`jsr305`) required by security crypto transitive code.

## [1.0.2] - 2026-05-11

### Changed

- Release retrigger after workflow cancellation to produce signed artifacts and publish GitHub Release from the corrected build pipeline.

## [1.0.0] - 2026-05-11

### Added

- First public release process for this fork under package com.johanpy.drivesoberly.
- Tag-based GitHub release workflow producing signed APK and SHA256 checksum.
- Documented release strategy for GitHub then F-Droid.

### Changed

- Unified CI branch targeting on master.
- Modernized legacy GitHub Actions workflows to JDK 17 and current action versions.
- Reset Android release metadata for first public release of this fork.

### Removed

- Deprecated Crashlytics lane from Fastlane configuration.
