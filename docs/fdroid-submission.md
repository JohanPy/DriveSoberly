# F-Droid Submission Guide

This fork is published as a separate application package: `com.johanpy.drivesoberly`.

## Goals

- keep the source tree reproducible from a tagged release
- avoid proprietary services and closed-source dependencies
- provide a clear path for F-Droid maintainers to build and review the app

## Current status

- no network permissions in the manifest
- no analytics SDK
- no crash reporting SDK
- release build minifies and shrinks resources
- automated unit tests and debug builds already pass in CI
- release `v1.2.2` is published and signed on GitHub Releases
- local F-Droid metadata file is prepared in `fdroid/metadata/com.johanpy.drivesoberly.yml`

## What F-Droid needs

1. A stable tag such as `v1.0.0`.
2. A clean source snapshot from that tag.
3. Build instructions that work from source.
4. Metadata describing the app, screenshots, and changelog.
5. A release process that does not depend on Google Play services.

## Recommended submission checklist

- verify `applicationId` is `com.johanpy.drivesoberly`
- verify the README mentions the fork and the release process
- verify the changelog is updated for the tagged release
- verify `ktlintCheck`, `testDebugUnitTest`, `assembleDebug`, and `connectedDebugAndroidTest` pass
- verify no proprietary or tracking dependencies are introduced
- verify screenshots reflect the current UI

## Submission package prepared in this repository

- package id: `com.johanpy.drivesoberly`
- metadata source: `fdroid/metadata/com.johanpy.drivesoberly.yml`
- current target version: `1.2.2` (`versionCode` 35)
- source tag for reproducible build: `v1.2.2`
- README screenshots are available in `screenshots/`

## Next steps to submit on F-Droid

1. Fork or clone the `fdroiddata` repository.
2. Copy `fdroid/metadata/com.johanpy.drivesoberly.yml` to `fdroiddata/metadata/com.johanpy.drivesoberly.yml`.
3. Open a merge request in `fdroiddata` with that metadata file and a short app description in the MR body.
4. Reply to maintainer feedback (usually around build reproducibility and description wording).
5. Once merged, monitor the first F-Droid build and indexing cycle.

## Metadata notes

Suggested F-Droid metadata entries should highlight:

- offline-first alcohol estimation
- zero permissions
- no tracking
- no proprietary APIs
- fork status and package name change

## Maintenance notes

- keep F-Droid-facing release notes short and user-oriented
- bump `versionCode` and `versionName` for every public release
- avoid depending on Google Play-only publishing steps for the F-Droid source tag
