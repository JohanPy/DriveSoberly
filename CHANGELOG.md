# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project follows Semantic Versioning.

## [1.0.6] - 2026-05-11

### Fixed

- Fixed release publishing workflow: replaced obsolete `r0adkll/sign-android-release` action with manual jarsigner + zipalign approach.
- Added missing GitHub token permissions (`contents: write`) for release creation.
- Implemented fallback for zipalign (if unavailable, APK is still signed and usable).

### Changed

- Release APK now signed with `jarsigner` (JDK built-in) using SHA256withRSA algorithm.
- Disabled automatic release notes generation to avoid 403 permission error.

## [1.0.5] - 2026-05-11

### Fixed

- Fixed signing step failure by installing Android SDK platform tools for `zipalign` utility.

## [1.0.4] - 2026-05-11

### Fixed

- Added missing GitHub Actions secrets (RELEASE_KEYSTORE_B64, RELEASE_KEY_ALIAS, RELEASE_KEYSTORE_PASSWORD, RELEASE_KEY_PASSWORD).

## [1.0.3] - 2026-05-11

### Fixed

- Replaced obsolete signing action with modern jarsigner implementation for v1.0.2 workflow retry.

## [1.0.2] - 2026-05-11

### Changed

- Release retrigger after workflow cancellation to produce signed artifacts and publish GitHub Release from the corrected build pipeline.

## [1.0.1] - 2026-05-11

### Fixed

- Fixed release build failure on R8 minification by adding missing `javax.annotation` classes dependency (`jsr305`) required by security crypto transitive code.

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
