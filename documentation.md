# Project Documentation

This document provides a comprehensive overview of the EDVO project, a Kotlin Multiplatform application using Jetpack Compose for its user interface.

## Project Structure

The project is organized into a multiplatform structure, allowing code to be shared across Android, iOS, and JVM (Desktop) targets.

- **`composeApp/`**: The main module containing the application code.
  - **`src/commonMain/kotlin/`**: This directory holds the core logic and UI components shared among all platforms.
    - `App.kt`: The primary shared Composable function that defines the main user interface.
    - `Greeting.kt`: A simple class that generates a platform-specific greeting message.
    - `Platform.kt`: Defines an `expect` interface and function (`getPlatform`) that requires a platform-specific implementation.
  - **`src/androidMain/kotlin/`**: Contains code specific to the Android platform.
    - `Platform.android.kt`: The `actual` implementation of the `getPlatform` function for Android, which reports the Android version.
    - `MainActivity.kt`: The entry point for the Android application, responsible for launching the Compose UI.
  - **`src/iosMain/kotlin/`**: Contains code specific to the iOS platform.
    - `Platform.ios.kt`: The `actual` implementation of `getPlatform` for iOS, reporting the iOS version.
    - `MainViewController.kt`: The entry point for the iOS application.
  - **`src/jvmMain/kotlin/`**: Contains code specific to the JVM (desktop) platform.
    - `Platform.jvm.kt`: The `actual` implementation of `getPlatform` for the JVM, reporting the Java version.
    - `main.kt`: The entry point for the desktop application.
- **`gradle/`**: Contains Gradle build-related files, including the `libs.versions.toml` file for dependency management.
- **`build.gradle.kts`**: The main build script for the root project.
- **`composeApp/build.gradle.kts`**: The build script for the `composeApp` module, defining dependencies for each source set (`commonMain`, `androidMain`, etc.).

## Architecture & Core Concepts

The project leverages key Kotlin Multiplatform features to maximize code reuse.

### Shared UI with Jetpack Compose

The entire user interface is built with Jetpack Compose and located in `commonMain`. The `App.kt` file serves as the root of the UI tree. This allows for a consistent look and feel across all target platforms without duplicating UI code.

### Platform-Specific Implementations (`expect`/`actual`)

To access platform-specific APIs and information, the project uses the `expect`/`actual` pattern.

- **`expect fun getPlatform(): Platform`** is declared in `commonMain`. This sets a requirement that every target platform must provide a function with this signature.
- **`actual fun getPlatform(): Platform`** is implemented in each target's source set (`androidMain`, `iosMain`, `jvmMain`), providing the concrete implementation needed to fulfill the `expect` declaration. This is how the app retrieves and displays the current platform's name (e.g., "Android 33", "iOS 16.2", "Java 17.0.1").

This architecture allows the shared `commonMain` code to call `getPlatform()` without needing to know the details of the underlying platform, promoting clean, decoupled code.

## How to Build and Run

- Use the Gradle wrapper (`./gradlew`) to build and run the application.
- Common tasks:
  - `./gradlew :composeApp:run` - Runs the desktop application.
  - `./gradlew :composeApp:installDebug` - Installs the Android application on a connected device/emulator.
  - The iOS app is typically run via Xcode.

This documentation should provide a solid foundation for any developer or LLM to understand and contribute to the project.
