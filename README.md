# EDVO - The Vault

**EDVO** is a high-security, local-first vault application built with **Kotlin Multiplatform** (Compose Multiplatform). It is designed to provide maximum privacy with an impenetrable UI.

## Core Features

*   **Zero-Knowledge Security**: Data is encrypted locally. No cloud sync.
*   **Neo-Terminal UI**: A distinct, high-contrast aesthetic inspired by retro-futurism (Green/Black).
*   **Fortified Editor**:
    *   **SecureTextField**: Prevents keyboard learning, paste attacks, and unauthorized selection.
    *   **Clipboard & Screenshot Blocking**: Toggleable protections to prevent data leaks.
    *   **Panic Mode**: Rapid data wipe capabilities.

## Tech Stack

*   **framework**: Kotlin Multiplatform (Android, iOS, Desktop/JVM)
*   **UI**: Compose Multiplatform with a custom `NeoTheme` design system.
*   **Architecture**: MVVM with Clean Architecture principles.
*   **Persistence**: SQLDelight (Local Database).
*   **Security**: `androidx.security.crypto` (Android) / Common Crypto (iOS).

## Getting Started

### Prerequisites
*   Android Studio / IntelliJ IDEA (Latest)
*   JDK 17+
*   Xcode (for iOS build)

### Build and Run

**Android**
```shell
./gradlew :composeApp:assembleDebug
```

**Desktop (JVM)**
```shell
./gradlew :composeApp:run
```

**iOS**
Open `iosApp/iosApp.xcodeproj` in Xcode and run.