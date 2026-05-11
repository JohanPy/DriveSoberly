# DriveSoberly

Alcohol blood rate computer for driving.

> Fork notice: this project is a fork of [VincentAudibert/DriveSoberly](https://github.com/VincentAudibert/DriveSoberly), originally created by Vincent Audibert. This fork modernizes dependencies, architecture, and BAC modeling while preserving license attribution.

## Get The App

- GitHub Releases (current distribution): <https://github.com/JohanPy/DriveSoberly/releases>
- F-Droid (planned for this fork package): `com.johanpy.drivesoberly`

See [docs/fdroid-submission.md](docs/fdroid-submission.md) for submission and release checklist details.

## About

DriveSoberly estimates whether you are fit to drive based on profile settings, consumed drinks, and legal limits.

## Features

The app displays:

- a clear status (safe to drive / not sober / do not drive)
- current estimated BAC
- estimated time to legal driving threshold
- estimated time to 0.00 g/L

The model considers:

- body weight
- sex
- declared tolerance profile
- legal context (country, young driver, professional driver)
- consumed drinks (volume, ABV, ingestion time)
- stomach state at drinking time

If your country is not available, select `OTHER` and define a custom legal BAC limit.

## BAC Model Summary

DriveSoberly uses a two-compartment pharmacokinetic model (stomach -> body) with linear hepatic elimination.

### Effective Distribution

Each gram of alcohol is diluted across an effective body weight:

$$W_{eff} = W \times r$$

| Sex    | Distribution ratio $r$ |
| :----- | :--------------------- |
| Male   | 0.70                   |
| Female | 0.60                   |

### Dynamic Absorption

The model integrates two equations with 5-minute Euler steps:

$$\frac{dA_{stomach}}{dt} = -k_a \cdot A_{stomach}$$

$$\frac{dA_{body}}{dt} = k_a \cdot A_{stomach} - \beta_{mass}$$

Where:

- $A_{stomach}$ is alcohol in stomach (g)
- $A_{body}$ is alcohol in body (g)
- $k_a$ is absorption rate (h$^{-1}$)
- $\beta_{mass} = \beta \times W_{eff}$ is elimination (g/h)

BAC at time $t$:

$$\text{BAC}(t) = \frac{A_{body}(t)}{W_{eff}}$$

### Food Impact

Food slows absorption and shifts peak BAC later.

| Stomach state | Example foods                    | $k_a$ (h$^{-1}$) | Lag $t_{lag}$ (h) | Typical impact                 |
| :------------ | :------------------------------- | :--------------- | :---------------- | :----------------------------- |
| Empty         | -                                | 2.0              | 0.25              | highest and earliest peak      |
| Light snack   | chips, fruit, crackers, cheese   | 1.2              | 0.40              | lower and delayed peak         |
| Full meal     | pasta, rice, pizza, meat, burger | 0.6              | 0.75              | much lower and later peak      |

## Safety Disclaimer

This app is not a medical or legal device. BAC is always an estimate. If in doubt, do not drive.

## Contributing

Contributions are welcome for:

- translations
- legal data corrections
- UI improvements
- code quality and tests

Open an issue or submit a pull request: <https://github.com/JohanPy/DriveSoberly/issues/new/choose>

## Run The Project

### Prerequisites

- Android Studio Ladybug or newer
- Java 17
- Android SDK platform 35

### Build Debug APK

```bash
./gradlew clean assembleDebug
```

### Run Quality Gate

```bash
./gradlew ktlintCheck testDebugUnitTest connectedDebugAndroidTest
```

### Build Release APK

```bash
./gradlew clean assembleRelease
```

Release signing is performed in CI for tagged releases.

## Release Versioning

- Tag format: `vMAJOR.MINOR.PATCH` (example: `v1.0.0`)
- `versionName` follows semantic versioning
- `versionCode` increments each public release
- main branch: `master`

## Release Process

1. Merge only when CI is fully green.
2. Update [CHANGELOG.md](CHANGELOG.md) and version metadata.
3. Push a SemVer tag to trigger signed release build.
4. Publish APK and SHA256 checksum in GitHub Release.
5. Submit the same tagged source to F-Droid.

## Changelog

Release notes are tracked in [CHANGELOG.md](CHANGELOG.md).
