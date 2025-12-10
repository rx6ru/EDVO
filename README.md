# EDVO - The Vault

![Version](https://img.shields.io/badge/version-v0.3.0-green) ![Platform](https://img.shields.io/badge/platform-Android%20-blue)

**EDVO** is a high-security, local-first vault application built with **Kotlin Multiplatform** (Compose Multiplatform). It combines uncompromising privacy with a distinct Neo-Brutalist aesthetic.

---

## 🛡️ Core Features

### 🔒 Maximum Security
*   **Zero-Knowledge Architecture**: Your data is encrypted locally (AES-256). No cloud sync, no tracking.
*   **Fortified Editor**: `SecureTextField` implementation prevents keyboard learning, clipboard capture, and unauthorized text selection.
*   **Release Hardening**: Automated ProGuard rules strip all logs in production builds to prevent data leakage.
*   **Panic Mode**: Rapidly wipe all data effectively using the "Kill Switch" slider.

### ⚡ Fluid Performance (v0.3.0)
*   **Just-in-Time Animations**: A rewritten motion engine delivers zero-lag scrolling with bouncy spring physics, animating items exactly as they enter the viewport.
*   **Native Optimization**: Uses platform-native implementations for date formatting and navigation handling for maximum efficiency.

### 🎛️ Advanced UX
*   **Multi-Selection Management**: Long-press to select notes, batch delete, and manage your vault with ease.
*   **Smart Navigation**: Integrated hardware back button support that understands context (clearing selections vs. navigation).
*   **Neo-Terminal UI**: A high-contrast, retro-futurist design system featuring "Signal Red" alerts and Monospace typography.

---

## 🛠️ Tech Stack

*   **Language**: Kotlin (100%)
*   **Framework**: Compose Multiplatform (Android, iOS, Desktop)
*   **Architecture**: MVVM + Clean Architecture
*   **Persistence**: SQLDelight (Local SQLite Database)
*   **Async**: Kotlin Coroutines & Flow
*   **State Management**: `remember` / `collectAsState`

---

## 🚀 Getting Started

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