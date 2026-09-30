# Dynamis

A weather app for Android and iOS, built with Kotlin Multiplatform and Compose Multiplatform. Weather comes from [Open-Meteo](https://open-meteo.com/); Compass provides device location and reverse geocoding.

## Structure

The app uses a single Gradle module. Shared application code lives under `org.xuan.dynamis`:

```text
composeApp/src/
  commonMain/
    kotlin/org/xuan/dynamis/
      App.kt
      data/
        location/          Compass adapter
        repo/              Repository implementation and forecast validation
        source/api/        Ktor API, HTTP configuration, and immutable DTOs
      domain/
        model/             Coordinates and weather forecasts
        LocationProvider.kt
        WeatherRepository.kt
      ui/
        screen/home/       Route, screen, ViewModel, state, and display mapping
        theme/             Material 3 light and dark palettes
      di/                  Application dependency graph and startup
    composeResources/      Shared strings
  androidMain/             Android Application, activity, manifest, and resources
  iosMain/                 Compose UIViewController entry point
  commonTest/              Mapping, location adapter, and HTTP regression tests
  androidUnitTest/         ViewModel and dependency lifetime tests
iosApp/                    SwiftUI host and Xcode project
```

## Architecture

| Layer | Responsibility |
| --- | --- |
| UI | `HomeRoute` obtains a navigation-scoped ViewModel and collects state with lifecycle awareness. `HomeScreen` renders state and invokes callbacks. |
| ViewModel | Coordinates refreshes and exposes one read-only `StateFlow<HomeUiState>` with loading, success, and recoverable error states. |
| Domain | Defines platform-independent models and weather/location contracts. It has no Compose, Ktor, serialization, or Compass dependencies. |
| Data | Implements domain contracts, handles platform location results, and validates API DTOs before returning forecasts. |
| DI | Starts Koin from the Android Application or iOS controller entry point. Dependencies live independently of UI composition. |

Weather requests are suspending functions. HTTP errors, parsing failures, and incomplete required readings reach the ViewModel's error state; cancellation propagates normally. Refresh cancels the previous load so old results cannot overwrite a new request.

Forecast timestamps retain the location's local time and timezone. Missing daily high/low values display as unavailable. Reverse geocoding is optional and bounded by a timeout: the forecast appears before its place name arrives.

Use cases can be introduced when business logic is shared or grows complex. Observable caching can be added behind `WeatherRepository` when offline weather becomes a requirement.

## Build and verification

Use JDK 21, Android SDK 36, and Android Studio with Kotlin Multiplatform support. Set `JAVA_HOME` for command-line builds or configure Android Studio's Gradle JDK. Android supports API 24 and above. The Android application ID is `org.xuan.project`; its source namespace is `org.xuan.dynamis`.

On Windows:

```powershell
.\gradlew.bat :composeApp:assembleDebug :composeApp:testDebugUnitTest :composeApp:lintDebug :composeApp:compileCommonMainKotlinMetadata
```

On macOS or Linux:

```sh
./gradlew :composeApp:assembleDebug :composeApp:testDebugUnitTest :composeApp:lintDebug :composeApp:compileCommonMainKotlinMetadata
```

The GitHub Actions workflow runs these checks on pushes to `develop`/`main` and pull requests. Test and lint reports are written to `composeApp/build/reports/`.

Tests cover forecast dates and incomplete readings, HTTP errors and cancellation, location denials/timeouts, loading and retry transitions, ViewModel clearing, and application dependency lifetime. Screen previews use sample state without accessing location or the network.

iOS builds require macOS and Xcode. Open `iosApp/iosApp.xcodeproj`, configure signing, and select an iOS device or simulator. Shared tests can also run with `./gradlew :composeApp:iosSimulatorArm64Test` on an Apple Silicon Mac.

After UI or platform changes, check permission denial/recovery, offline retry, theme switching, rotation, and large font sizes on a device or simulator.

Dependency versions are pinned in `gradle/libs.versions.toml`. Upgrade Kotlin, Compose, AGP, and Gradle as a compatible set, then run the verification commands above.
