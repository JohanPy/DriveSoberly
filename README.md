# DriveSoberly
Alcohol blood rate computer for driving

> **Fork notice** — This project is a fork of [VincentAudibert/CanIDrive](https://github.com/VincentAudibert/CanIDrive), originally created by Vincent Audibert. The upstream project appears to be no longer maintained. This fork started by updating dependencies to resolve Android installation warnings, then evolved into a significant rewrite with new features, architecture improvements, and a more accurate BAC model. All original work is credited to Vincent Audibert under the original license.

## 📲 Get the app

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/com.vaudibert.canidrive/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=com.vaudibert.canidrive)

## 👋 About
DriveSoberly is an Android app letting you evaluate whether you can drive or not.

This project is a fork of [CanIDrive](https://github.com/VincentAudibert/CanIDrive), aimed at keeping the app functional on modern Android versions and improving its accuracy and usability.

## ✨ Features
Can I Drive allows you to track your recent alcohol consumption, to assess if you can drive or not.

What is shown to you :
- a clear status (OK, OK but not sober, NO GO)
- your estimated blood rate
- time when you can drive again
- time when you get sober again (0.0 g/L)

Computation takes into account :
- weight
- sex
- rough health indication
- driver legal status (country, young driver, professional)
- drinks (how much alcohol and when)
- stomach state at the time of drinking

If the chosen country is not available, you can select OTHER to override the threshold value with your own.

## 🧮 How the BAC is calculated

DriveSoberly uses a **two-compartment pharmacokinetic model** to estimate Blood Alcohol Concentration (BAC) over time. This is more realistic than the classic Widmark formula, which assumes alcohol is absorbed instantly.

### Widmark body distribution

Each gram of alcohol is diluted across an *effective body weight* that depends on sex:

$$W_{eff} = W \times r$$

| Sex | Distribution ratio $r$ |
|-----|------------------------|
| Male | 0.70 |
| Female | 0.60 |

### Two-compartment absorption model

Alcohol first enters the **stomach** compartment, then progressively passes into the **body** (bloodstream) compartment, where the liver eliminates it.

The system is governed by two differential equations, integrated numerically with 5-minute Euler steps:

$$\frac{dA_{stomach}}{dt} = -k_a \cdot A_{stomach}$$

$$\frac{dA_{body}}{dt} = k_a \cdot A_{stomach} - \beta_{mass}$$

where:
- $A_{stomach}$ — alcohol remaining in the stomach (g)
- $A_{body}$ — alcohol in the body (g)
- $k_a$ — gastric absorption rate constant (h⁻¹), controlled by food state
- $\beta_{mass} = \beta \times W_{eff}$ — hepatic elimination rate (g/h); $\beta$ ranges from 0.085 to 0.15 g/L/h depending on sex and declared tolerance

BAC at any moment is then:

$$\text{BAC}(t) = \frac{A_{body}(t)}{W_{eff}}$$

### Effect of food on absorption

Food slows gastric emptying, which delays and reduces the BAC peak. Two parameters are adjusted depending on the declared stomach state:

| Stomach state | Examples | $k_a$ (h⁻¹) | Lag time $t_{lag}$ (h) | Effect on peak BAC |
|---|---|---|---|---|
| Empty | – | 2.0 | 0.25 | Highest, earliest (~30–60 min) |
| Light snack | chips, fruit, cheese, crackers | 1.2 | 0.40 | ~20–30 % lower, ~30–45 min later |
| Full meal | pasta, rice, pizza, meat, burger | 0.6 | 0.75 | ~30–40 % lower, ~60–90 min later |

Alcohol absorption only begins after the lag time $t_{lag}$, which represents how long the stomach holds the alcohol before it reaches the intestine (where rapid absorption occurs).

### Hepatic elimination

Once the stomach is empty, elimination becomes purely linear:

$$\text{BAC}(t) = \text{BAC}_{peak} - \beta \cdot (t - t_{peak})$$

The model uses this shortcut to compute the *time to reach a given limit* efficiently without stepping through every 5-minute interval.

### Limitations

This model gives a reasonable approximation but **is not a medical device**. Individual absorption rates vary with genetics, hydration, medication, and other factors. Always err on the side of caution.

## 🙋 Contributing
Contributions are welcome:
- translating the app & presentation text in your language
- legal data corrections (BAC limits by country)
- improving UI
- code

For any contribution, please [open an issue](https://github.com/JohanPy/DriveSoberly/issues/new/choose), or clone & submit a pull request 🙂.

## ▶️ Run the project
* clone the repo
* open it with Android Studio
* have fun with it...

---

## 📋 Changelog

All changes listed here were made in this fork, after the upstream project ([VincentAudibert/CanIDrive](https://github.com/VincentAudibert/CanIDrive)) became inactive.

### Bug fixes
- Fixed crash on small alcohol rates (`substring(0, 4)` → `String.format("%.2f g/L")`)
- Fixed crash on invalid weight/degree input (`toDouble()` → `toDoubleOrNull()` with fallback)
- Fixed Saint Patrick's Day detection (wrong month field, wrong day logic)
- Fixed `equals`/`hashCode` contract violation in `PresetDrink` (`count` removed from `hashCode`)
- Fixed destructive database migration 1→2: table rename preserved, `PresetDrinkEntity` creation added, `fallbackToDestructiveMigration()` removed
- Fixed BAC unit inconsistency in `DriveLaws`: all country limits now standardised to g/L (was mixing g/L and g/dL)
- Fixed `LiveData` observer leak in `PresetDrinksAdapter`: observation moved from `getView()` to `init`
- Fixed `Date.month` / `Date.day` deprecated API usage (migrated to `Calendar`)
- Fixed `String.toUpperCase()` without Locale (`uppercase(Locale.ROOT)`)

### Architecture
- Introduced ViewModels (`DriveViewModel`, `DrinkerViewModel`, `AddDrinkViewModel`) with `viewModelScope`
- Introduced Koin dependency injection — replaced global `CanIDrive` Application singleton
- Moved repositories from `ui.repository` to `data.repository`
- Removed Android resource (`R.string`) references from the domain layer
- Fixed Law of Demeter violations in `IngestedDrinksAdapter` (4-level deep chain replaced by constructor injection)
- Replaced mutable lambda callbacks with `StateFlow` (`PhysicalBody`, `DriveLawService`, `PresetDrinkService`)
- Extracted country BAC data from `DriveLaws.kt` into a bundled `drive_laws.json` asset; `R.string` references resolved in a mapper class

### Security
- Enabled minification and resource shrinking for release builds
- Migrated all SharedPreferences to `EncryptedSharedPreferences` (AES256-GCM) for health data
- Added `android:fullBackupContent` / `android:dataExtractionRules` to exclude sensitive preferences from cloud backup
- Added legal disclaimer `AlertDialog` shown on first launch; accepted state persisted

### Performance
- Migrated `ListView` to `RecyclerView` + `ListAdapter` + `DiffUtil` in drink history and preset lists
- Implemented `RecyclerView.Adapter` with `ViewHolder` pattern (fixing excessive GC from `BaseAdapter`)
- Coroutines now managed via `viewModelScope` — no more leaked `Job()` / manual `CoroutineScope`

### UI / UX
- Migrated to Android 12+ `SplashScreen` API
- Added deletion confirmation dialogs for preset and history drinks
- Extended weight picker to free-text `EditText` input (no more fixed `NumberPicker` range)
- Wired up the Settings screen (`SettingsFragment` + nav graph integration)
- Added Up/Back button in Toolbar via `setupWithNavController`
- Implemented dark mode (`values-night/colors.xml` + `DayNight` theme)
- Added "Safe to drive" / "DO NOT DRIVE" explicit text labels in `DriveFragment`
- Added `LabelFor` on the custom BAC limit `EditText` in the country screen
- Replaced `android:tint` with `app:tint` on `ImageView` for `AppCompatImageView` backward compat
- Replaced deprecated `toggleSoftInput` with `WindowInsetsController`

### Features added
- **Stomach state selector** — toggles *Empty / Light snack / Full meal* on the main screen; affects the pharmacokinetic absorption model
- **Two-compartment BAC model** — replaced instant Widmark formula with a step-based pharmacokinetic simulation (5-min Euler steps, gastric absorption rate k_a and lag time t_lag per food state)
- **Country BAC limit validation test** — unit test asserting expected limits for ~65 countries as a regression guard

### Internationalisation
- Removed hardcoded `"average"` string in pickers (now `@string/alcohol_tolerance_medium`)
- Removed redundant `translatable="false"` keys from Italian strings
- Locale-aware number formatting (`NumberFormat.getInstance(locale)` instead of `DecimalFormat("0.#")`)

### Testing
- Fixed JUnit 4/5 mixing in `PresetDrinkServiceTest`
- Added `DrinkerStatusServiceTest`, `IngestionServiceTest`, `TimeServiceAndroidTest`, `FoodStateTest`
- Added tests for `IngestedDrink.alcoholMass()` and Room `Converters`
- Added Espresso happy-path UI test (`HappyPathUITest`)
- Added `org.json:json` pure-Java dependency to fix `org.json.JSONArray` unavailable in JVM unit tests

### Build & tooling
- Moved `kotlin-reflect` to `testImplementation`
- Removed unused `legacy-support-v4` dependency
- Disabled Jetifier (not needed with pure AndroidX)
- Added GitHub Actions CI/CD workflow
- Integrated `ktlint` for code formatting and linting

