# EDVO - The Vault

![Version](https://img.shields.io/badge/version-v0.4.0-green) ![Platform](https://img.shields.io/badge/platform-Android%20|%20Desktop%20|%20iOS-blue) ![License](https://img.shields.io/badge/license-MIT-purple)

**EDVO** is an uncompromising, local-first vault application built with **Kotlin Multiplatform** (Compose Multiplatform). It fuses military-grade security with a distinct Neo-Brutalist design language (`NeoPaletteV2`), designed for those who demand absolute privacy and zero-knowledge architecture.

---

## 🛡️ Core Features

### 🔒 Maximum Security
*   **Zero-Knowledge Architecture**: Your data is encrypted locally (AES-256 / ChaCha20). No cloud sync, no tracking, no exceptions.
*   **Fortified Editor**: `SecureTextField` implementation prevents keyboard learning, clipboard capture, and unauthorized text selection.
*   **Release Hardening**: Automated ProGuard rules strip all logs in production builds.
*   **Panic Mode**: Rapidly wipe all data effectively using the "Kill Switch" slider.

### 💾 Robust Data Management (v0.4.0)
*   **Encrypted Backups**: Export your entire vault as a strongly encrypted file (`.enc`).
*   **Seamless Restoration**: Import backups with visual feedback.
*   **Feedback UX**: New `NeoFeedbackOverlay` system provides clear, animated confirmation (Success/Error) during critical operations.

### ⚡ Fluid Performance
*   **Just-in-Time Animations**: A rewritten motion engine delivers zero-lag scrolling with bouncy spring physics.
*   **Hero Transitions**: Smooth, physics-based morphing animations for authentication screens.
*   **Native Optimization**: Platform-native implementations for crypto and IO operations.

### 🎛️ Advanced UX
*   **Neo-Terminal Design**: High-contrast, retro-futurist aesthetic featuring "Signal Green" and "Signal Red" status indicators.
*   **Multi-Selection**: Long-press to select notes, batch delete, and manage your vault.
*   **Smart Navigation**: Context-aware hardware back button support.

---

## 🛠️ Tech Stack

*   **Language**: Kotlin (100%)
*   **Framework**: Compose Multiplatform (Android, iOS, Desktop)
*   **Architecture**: MVVM + Clean Architecture based on UDF (Unidirectional Data Flow)
*   **Persistence**: SQLDelight (Local SQLite Database)
*   **Cryptography**: Tink / Android Keystore / CryptoKit
*   **Async**: Kotlin Coroutines & Flow
*   **IO**: FileKit for cross-platform file handling

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer) / IntelliJ IDEA
*   JDK 17+
*   Xcode 15+ (for iOS build)

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

---

## 🤝 Contributing

Contributions are welcome! Please ensure you:
1.  Follow the Neo-Brutalist design guidelines.
2.  Maintain the zero-knowledge security standard.
3.  Write tests for new use cases.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.