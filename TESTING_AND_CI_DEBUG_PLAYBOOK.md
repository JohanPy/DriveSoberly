# DriveSoberly Testing and CI Debug Playbook

Audience: developers and coding agents working on CI, Android tests, and release validation.

Goal: reduce CI round-trips by validating locally first and using a repeatable debug workflow.

## 1) Quick Start

Use Android Studio JBR locally (required for stable Gradle + javac availability):


Recommended one-shot local validation before pushing:


If this passes locally, push and monitor GitHub Actions.

## 2) CI Workflows to Keep Green

Main workflows on push to master:


Minimum release gate:


## 3) Local Validation Checklist

Run in this order:

1. Style and static formatting

2. Unit tests

3. Build debug APK

4. Instrumented tests on device/emulator

If you need to auto-fix formatting first:


## 4) Known CI and Test Failure Patterns

### A) GitHub Actions Node deprecation warnings

Symptom:

Fix:

### B) Emulator install or boot instability (instrumented CI)

Symptoms:

Stabilization settings used successfully:


### C) Espresso interaction flakes

Symptoms:

Practical mitigations:


### D) RecyclerView item click selecting wrong row

Root cause seen in this project:

Fix:

### E) Locale-sensitive spinner assertions

Symptom:

Fix:

### F) Country list custom option not visible

Root cause seen in repository logic:

Fix applied:

## 5) Instrumented Test Design Rules for This Repo


## 6) Debug Procedure When Android instrumented CI Fails

1. Reproduce locally first with connectedDebugAndroidTest.
2. Open report:

3. Class-level detail page:

4. Identify exact failing interaction type:

5. Patch test for determinism:

6. Re-run full local gate before push:

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


Inspect one run:


API status snapshot:


Check jobs in a run:


## 9) Current Project-Specific Notes


## 10) Ownership and Updates

This file should be updated whenever:

