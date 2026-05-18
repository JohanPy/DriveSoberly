# DriveSoberly Testing and CI Debug Playbook

Audience: developers and coding agents working on CI, Android tests, and release validation.

Goal: reduce CI round-trips by validating locally first and using a repeatable debug workflow.

## 1) Quick Start

Use Android Studio JBR locally (required for stable Gradle + javac availability):

- `JAVA_HOME=/home/johan/programme/Android/android-studio/jbr`
- `PATH=$JAVA_HOME/bin:$PATH`

Recommended one-shot local validation before pushing:

- `./gradlew ktlintCheck testDebugUnitTest assembleDebug connectedDebugAndroidTest --no-daemon`

If this passes locally, push and monitor GitHub Actions.

## 2) CI Workflows to Keep Green

Main workflows on push to master:

- Android CI
- Android test
- Android build debug
- Android instrumented CI

Minimum release gate:

- all four workflows completed with success on the release commit
- prefer one additional clean rerun if there was recent flakiness on instrumented tests

## 3) Local Validation Checklist

Run in this order:

1. Style and static formatting

   - `./gradlew ktlintCheck --no-daemon`

2. Unit tests

   - `./gradlew testDebugUnitTest --no-daemon`

3. Build debug APK

   - `./gradlew assembleDebug --no-daemon`

4. Instrumented tests on device/emulator

   - `./gradlew connectedDebugAndroidTest --no-daemon`

If you need to auto-fix formatting first:

- `./gradlew ktlintFormat --no-daemon`

## 4) Known CI and Test Failure Patterns

### A) GitHub Actions Node deprecation warnings

Symptom:

- warnings about Node 20 deprecation

Fix:

- use `actions/checkout@v5`
- use `actions/setup-java@v5`

### B) Emulator install or boot instability (instrumented CI)

Symptoms:

- `adb exit 224`
- install verification failures
- `Failed to install split APK(s)`
- `Unknown failure: cmd: Can't find service: package`
- very long startup or no test execution

Stabilization settings used successfully:

- api-level: 34
- target: `google_apis`
- arch: `x86_64`
- profile: `Nexus 6`
- emulator options: `-no-window -gpu swiftshader_indirect -noaudio -no-boot-anim`
- emulator boot timeout: 900
- wait explicitly for `sys.boot_completed`, `dev.bootcomplete`, and Package Manager readiness (`adb shell pm path android`) before running Gradle tests
- disable animations and package verification before running tests

Important script compatibility note:

- in `android-emulator-runner` script blocks, prefer one-line `until ...; do ...; done` loops; multiline loops can be split by the runner and cause `/usr/bin/sh` syntax errors
- keep pre-test emulator input minimal; avoid unnecessary `HOME/BACK/tap` keyevent sequences that can leave the app without a focused root window

### C) Espresso interaction flakes

Symptoms:

- `RootViewWithoutFocusException`
- `NoMatchingViewException` for views that are present manually

Practical mitigations:

- disable system animations in test setup via UiAutomation shell commands
- ensure wake + keyguard dismissal on CI emulator before tests
- add explicit intermediate assertions across fragment transitions
- avoid opening transient system menus in test setup (`KEYCODE_82` can create popup overlays and break root matching)
- avoid asserting optional status panels that are hidden with no ingested drink (for example sober projection container)

### D) RecyclerView item click selecting wrong row

Root cause seen in this project:

- position 0 in preset list is an add preset cell, not the first drink preset

Fix:

- click position 1 for the first real preset item
- avoid ambiguous direct matching on repeated child view ids

### E) Locale-sensitive spinner assertions

Symptom:

- test searching for `OTHER` fails on non-English locale

Fix:

- read localized label from resources in tests (`R.string.other`)
- never hardcode locale-specific display strings in Espresso `onData` assertions

### F) Country list custom option not visible

Root cause seen in repository logic:

- default drive law entry was not included in spinner source list

Fix applied:

- include `DriveLaws.default` in the list passed to `DriveLawService`

## 5) Instrumented Test Design Rules for This Repo

- keep each test focused on one high-value flow
- avoid coupling two unstable interactions in one test when one assertion can validate the target behavior
- do not depend on previous test state or selected preset persistence
- prefer resource-based matching over literal text when localization is involved
- when CI is unstable, keep only the lowest-variance smoke path in instrumented tests first
- for this repo, spinner selection + localized AdapterView matching + return navigation is the first interaction bundle to remove
- validate country-limit logic in JVM tests before reintroducing any UI coverage for that path

## 6) Debug Procedure When Android instrumented CI Fails

1. Reproduce locally first with connectedDebugAndroidTest.

2. Open report:

   - `app/build/reports/androidTests/connected/debug/index.html`

3. Class-level detail page:

   - `app/build/reports/androidTests/connected/debug/com.johanpy.drivesoberly.HappyPathUITest.html`

4. Identify exact failing interaction type:

   - missing view
   - wrong data in AdapterView
   - click failure due to visibility or focus
   - install failure before any test starts

5. Patch test for determinism:

   - assert fragment transitions explicitly
   - use correct RecyclerView position
   - localize spinner matching
   - stabilize emulator readiness checks

6. Re-run full local gate before push:

   - `ktlintCheck + testDebugUnitTest + assembleDebug + connectedDebugAndroidTest`

7. Push and monitor all workflows.

## 7) Release Replacement Procedure (v1.2.0-style)

Only do this after all workflow gates are green.

Suggested steps:

1. verify local full gate is green
2. verify all four GitHub workflows are green for target commit
3. delete old release tag and release object for the version
4. recreate the release from the final validated commit
5. verify assets and metadata

## 8) Useful Commands

Check recent runs:

- `gh run list --limit 12`

Inspect one run:

- `gh run view <run_id>`

API status snapshot:

- `gh api repos/JohanPy/DriveSoberly/actions/runs/<run_id> --jq '{status:.status,conclusion:.conclusion,updated_at:.updated_at,html_url:.html_url}'`

Check jobs in a run:

- `gh api repos/JohanPy/DriveSoberly/actions/runs/<run_id>/jobs`

## 9) Current Project-Specific Notes

- `ktlint` must stay green before any CI push
- instrumented tests are the slowest and most failure-prone stage
- prefer local full validation to reduce back-and-forth debugging
- if one workflow appears stale, trigger a fresh CI cycle from a new commit and track only that commit hash

## 10) Ownership and Updates

This file should be updated whenever:

- a new recurring failure mode is discovered
- emulator settings or workflow versions change
- test architecture changes (new suites, new runners, new tooling)
