# TODO – CanIDrive

Items marked ~~like this~~ were fixed in the initial implementation pass.

---

## CRITICAL – Bugs / Crashes ✓ All Fixed

- [x] ~~**Fix `substring(0, 4)` crash on small alcohol rates**~~ – `DriveFragment.kt`: replaced with `String.format("%.2f g/L", rate)`.
- [x] ~~**Fix `toDouble()` crash on invalid input**~~ – `DrinkerFragment.kt`: replaced with `toDoubleOrNull()` + fallback.
- [x] ~~**Fix Saint Patrick's Day logic (wrong month + wrong field)**~~ – `TimeServiceAndroid.kt`: migrated to `Calendar` with `MONTH`/`DAY_OF_MONTH`.
- [x] ~~**Fix `equals/hashCode` contract violation**~~ – `PresetDrink.kt`: removed `count` from `hashCode()` to match `equals()`.

---

## HIGH – Data Integrity

- [x] ~~**Fix destructive database migration**~~ – `MainRepository.kt`: migration `1→2` fixed and re-enabled. Added `NOT NULL DEFAULT ''` on `name` column, preserved the table rename, and added the missing `PresetDrinkEntity` table creation. `fallbackToDestructiveMigration()` removed.
- [x] ~~**Fix unit inconsistency for US drive laws**~~ – `DriveLaws.kt`: USA is `0.8` while Canada is `0.08`. Verify all ~60 country entries against official sources and standardize to g/L.
- [x] ~~**Validate all country BAC limits**~~ – Add a unit test that asserts expected BAC limit per country against a reference table to prevent accidental data corruption.

---

## HIGH – Architecture

- [x] ~~**Introduce ViewModels**~~ – No `ViewModel` is used anywhere. Fragments access `CanIDrive.instance.mainRepository` directly. Create `DriveViewModel`, `DrinkerViewModel`, `AddDrinkViewModel` with `viewModelScope`-bound coroutines.
- [x] ~~**Introduce Dependency Injection (Hilt or Koin)**~~ – Replace the global `CanIDrive` Application singleton with a proper DI framework. Improves testability and reduces coupling.
- [x] ~~**Move repositories out of `ui` package**~~ – `MainRepository`, `DrinkRepository`, `DigestionRepository`, `DriveLawRepository` are in `ui.repository`. Moved to `data.repository`.
- [x] ~~**Remove Android resource dependencies from domain layer**~~ – `DriveLaw.kt` holds `explanationId: Int` (an `R.string` reference). The domain should be pure Kotlin; resolve resource IDs in the UI layer only.
- [x] ~~**Fix Law of Demeter violations**~~ – `IngestedDrinksAdapter.kt`: `CanIDrive.instance.mainRepository.drinkRepository.ingestionService` is 4 levels deep. Expose needed services directly via constructor injection.

---

## HIGH – Legal / Safety

- [x] ~~**Add legal disclaimer**~~ – Disclaimer AlertDialog shown on first launch (`SplashFragment.kt`). Accepted state persisted in SharedPreferences.

---

## HIGH – Security

- [x] ~~**Enable minification/obfuscation for release builds**~~ – `app/build.gradle.kts`: `isMinifyEnabled = true` + `isShrinkResources = true` enabled for release.
- [x] ~~**Use `EncryptedSharedPreferences` for health data**~~ – All three repositories (`DigestionRepository`, `DriveLawRepository`, `MainRepository`) now use `EncryptedSharedPreferences` (AES256-GCM) via `androidx.security:security-crypto:1.1.0-alpha06`.
- [x] ~~**Review `allowBackup`**~~ – Added `android:fullBackupContent="@xml/backup_rules"` (API 23–30) and `android:dataExtractionRules="@xml/data_extraction_rules"` (API 31+) to exclude SharedPreferences from cloud backup while keeping the Room database backed up.

---

## MEDIUM – Performance

- [x] **Replace `ListView` with `RecyclerView`** – `constraint_content_drive_history.xml` and `constraint_content_add_drink_presets.xml`: migrate to `RecyclerView` + `ListAdapter` + `DiffUtil` for better scroll performance.
- [x] **Implement ViewHolder pattern in adapters** – `PresetDrinksAdapter.kt` and `IngestedDrinksAdapter.kt` extend `BaseAdapter` and inflate a new view on every `getView()` call, ignoring `convertView`. This causes excessive GC. Migrate to `RecyclerView.Adapter`.
- [x] ~~**Fix LiveData observer leak in `PresetDrinksAdapter`**~~ – Moved `liveSelectedPreset` observation from `getView()` into `init` block. State cached in a field.
- [x] **Cancel coroutines properly** – `DrinkRepository.kt`: `Job()` and manual `CoroutineScope` are never cancelled → memory/coroutine leaks. Use `viewModelScope` after ViewModel introduction, or implement `Closeable`.

---

## MEDIUM – Deprecated APIs

- [x] ~~**Replace deprecated `Date.month` / `Date.day`**~~ – `TimeServiceAndroid.kt`: migrated to `Calendar`.
- [x] ~~**Replace `String.toUpperCase()` without Locale**~~ – `DriveLawService.kt`: replaced with `uppercase(Locale.ROOT)`.
- [x] **Replace `toggleSoftInput(SHOW_FORCED, 0)`** – `KeyboardUtils.kt`: deprecated since API 31. Use `WindowInsetsController.show(WindowInsetsCompat.Type.ime())` instead.
- [x] **Replace `android:tint` with `app:tint`** in relevant `ImageView` XML attributes for backward compat with `AppCompatImageView` (affects `fragment_drive_status.xml` and `constraint_content_drinker_country.xml`).

---

## MEDIUM – Code Quality

- [x] ~~**Replace magic strings for sex with `Sex` enum**~~ – Created `Sex.kt` enum in `domain.digestion`. Updated `PhysicalBody`, `DigestionRepository`, `DrinkerFragment`, all test files.
- [x] ~~**Move `KeyboardUtils` to `ui.util` package**~~ – Moved to `ui/util/KeyboardUtils.kt`, updated all 3 import sites.
- [x] ~~**Remove unused `volume` and `degree` variables**~~ – `AddDrinkFragment.kt`.
- [x] **Replace mutable lambda callbacks with `Flow`/`LiveData`** – `PhysicalBody.onUpdate`, `DriveLawService.onCustomLimitCallback`, `PresetDrinkService.onPresetsChanged`: fragile `var` lambdas. Replace with `StateFlow` or `LiveData`.
- [x] **Separate `DriveLaws.kt` data from Android resources** – 361-line file mixes raw BAC data with `R.string.*` references. Consider loading from a bundled JSON asset file and resolving string IDs in a mapper class.

---

## MEDIUM – Testing

- [x] ~~**Fix JUnit 4/5 mixing**~~ – `PresetDrinkServiceTest.kt`: replaced `org.junit.Test` with `org.junit.jupiter.api.Test`.
- [x] **Add tests for `DrinkerStatusService`** – `DrinkerStatusServiceTest.kt` added.
- [x] **Add tests for `IngestionService`** – `IngestionServiceTest.kt` added.
- [x] **Add tests for `IngestedDrink.alcoholMass()`** – Critical BAC calculation has no direct unit test.
- [x] **Add tests for Room `Converters`** – `Date` ↔ `Long` conversion is untested; a regression could silently corrupt all timestamps.
- [x] **Add tests for `TimeServiceAndroid`** – `TimeServiceAndroidTest.kt` added.
- [ ] **Add repository tests** – No repository layer has any tests.
- [x] **Add UI / integration tests (Espresso)** – `HappyPathUITest.kt` added for the core user flow.
- [x] ~~**Validate `DriveLaws` data in tests**~~ – Assert expected BAC limits for a subset of countries as a regression guard.

---

## LOW – Accessibility

- [x] ~~**Move hardcoded `contentDescription` to string resources**~~ – `item_preset_drink.xml`, `item_past_drink.xml` now use `@string/content_desc_*`.
- [x] ~~**Add `contentDescription` to all FloatingActionButtons**~~ – All FABs in `fragment_drive_status.xml`, `fragment_drinker.xml`, `fragment_add_preset.xml` now have descriptions.
- [x] ~~**Fix splash screen text size unit**~~ – `fragment_splash.xml`: changed from `20pt` to `40sp`.
- [x] **Add explicit text labels for drive status** – `DriveFragment.kt`: added "Safe to drive" / "DO NOT DRIVE" labels.
- [x] **Fix suppressed `LabelFor` warning** – `constraint_content_drinker_country.xml`: `tools:ignore="LabelFor"` on the custom limit `EditText`. Add a proper `<TextView android:labelFor="@id/editTextCurrentLimit">`.

---

## LOW – Internationalization

- [x] ~~**Remove hardcoded `"average"` text**~~ – `constraint_content_drinker_pickers.xml`: replaced with `@string/alcohol_tolerance_medium`.
- [x] ~~**Remove redundant `translatable="false"` keys from Italian strings**~~ – Cleaned `values-it-rIT/strings.xml`.
- [x] **Hardcoded unit string `"g/L"`** – `constraint_content_drinker_country.xml`: move to string resource; consider supporting g/dL, mg/100mL, ‰ per country selection.
- [x] **Use locale-aware number formatting** – `DecimalFormat("0.#")` ignores locale decimal separator conventions. Use `NumberFormat.getInstance(locale)`.

---

## LOW – UI/UX Improvements

- [x] **Replace custom splash with Android 12+ SplashScreen API** – `MainActivity.kt`: migrated to `androidx.core:core-splashscreen`.
- [x] **Add deletion confirmation dialogs** – Drink delete buttons (presets and history) have no confirmation. Accidental taps permanently delete data. Show an `AlertDialog` before deletion.
- [x] **Extend weight picker range** – `DrinkerFragment.kt`: replaced `NumberPicker` with flexible `EditText` input.
- [x] ~~**Implement Settings screen**~~ – `menu_main.xml` has a Settings item but `MainActivity.kt` returns `true` without navigating. Wire up a `PreferenceFragment` or custom settings screen. Implementation added with `SettingsFragment.kt` and `nav_graph.xml` integration.
- [x] **Add Up/Back button in Toolbar** – `MainActivity.kt`: integrated `setupWithNavController` for standard navigation.
- [x] **Support dark mode** – `values-night/colors.xml` and `DayNight` theme implemented.

---

## LOW – Build & Configuration

- [x] ~~**Move `kotlin-reflect` to `testImplementation`**~~ – Only needed at test time.
- [x] ~~**Remove `legacy-support-v4` dependency**~~ – Unnecessary with `minSdk = 21` + AndroidX.
- [x] ~~**Disable Jetifier**~~ – `gradle.properties`: `android.enableJetifier` commented out; not needed with pure AndroidX deps.
- [ ] **Update Room to latest stable** – Current: `2.6.1`. Update to `2.7.x+`.
- [x] **Add CI/CD pipeline** – GitHub Actions workflow `android.yml` configured.
- [x] **Add `ktlint` or `detekt`** – `ktlint` integrated for formatting and linting.

---

## FEATURE IDEAS

- [ ] **Notification / alarm when user can drive again** – Calculate time until BAC drops below legal limit; schedule a `WorkManager` notification.
- [ ] **Home screen widget** – Quick-glance widget showing current BAC status and estimated time remaining.
- [ ] **BAC evolution graph** – Chart the alcohol rate over time (e.g., MPAndroidChart or Compose charts).
- [ ] **Food intake tracking** – Food slows alcohol absorption. Let user mark whether they ate before/during drinking to improve estimate accuracy.
- [ ] **Multiple BAC unit support** – Support g/dL, mg/100mL, ‰, and % BAC; auto-select or let user choose.
- [ ] **Data export / import** – Export drink history as CSV or JSON; re-import on a new device.
- [ ] **Bulk history deletion** – "Clear all history" or date-range deletion.
- [ ] **Favorite / recent drinks shortcuts** – One-tap re-add of last consumed drink from the main screen.
- [ ] **Hydration reminders** – Suggest drinking water between alcoholic drinks.
- [ ] **Taxi / rideshare quick link** – One-tap shortcut to a taxi or rideshare app when user cannot drive.
- [ ] **Onboarding flow** – First-launch tutorial explaining profile setup and how BAC estimation works.
- [ ] **Material Design 3 / Material You** – Migrate to Material 3 components and support dynamic color theming.
- [ ] **Jetpack Compose migration** – Migrate XML layouts to Compose for a modern, declarative UI.
- [ ] **Multi-user profiles** – Track multiple people (e.g., a group of friends, designated driver checking on passengers).
- [ ] **Weekly/monthly statistics** – Dashboard showing consumption trends over time.
- [ ] **Wear OS companion app** – Quick BAC status check from a smartwatch.
- [ ] **Health Connect / Google Fit integration** – Sync BAC and consumption data with health platforms.
