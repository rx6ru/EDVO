# EDVO - The Vault

![Version](https://img.shields.io/badge/version-v0.5.0-green) ![Platform](https://img.shields.io/badge/platform-Android%20|%20Desktop%20|%20iOS-blue) ![License](https://img.shields.io/badge/license-MIT-purple)

**EDVO** is an uncompromising, local-first vault application built with **Kotlin Multiplatform** (Compose Multiplatform). It fuses military-grade security with a distinct Neo-Brutalist design language (`NeoPaletteV2`), designed for those who demand absolute privacy and zero-knowledge architecture.

---

## 🛡️ Core Features

### 🔒 Maximum Security
*   **Zero-Knowledge Architecture**: Your data is encrypted locally (AES-256 / ChaCha20). No cloud sync, no tracking, no exceptions.
*   **Biometric Authentication**: Fingerprint unlock with secure fallback to master password.
*   **Fortified Editor**: `SecureTextField` implementation prevents keyboard learning, clipboard capture, and unauthorized text selection.
*   **Release Hardening**: Automated ProGuard rules strip all logs in production builds.
*   **Panic Mode**: Rapidly wipe all data using the "Kill Switch" slider.

### 💾 Robust Data Management
*   **Encrypted Backups**: Export your entire vault as a strongly encrypted file (`.enc`).
*   **Seamless Restoration**: Import backups with visual feedback.
*   **Password Rotation**: Securely change your master password with automatic re-encryption of all stored data.

### ⚡ Fluid Performance
*   **Optimized Navigation**: Swipe-based navigation between screens with smooth pager animations.
*   **Hero Transitions**: Physics-based morphing animations for authentication screens.
*   **Native Optimization**: Platform-native implementations for crypto and IO operations.

### 🎛️ Advanced UX
*   **Neo-Terminal Design**: High-contrast, retro-futurist aesthetic featuring "Signal Green" and "Signal Red" status indicators.
*   **Multi-Selection**: Long-press to select entries, batch delete, and manage your vault.
*   **Smart Navigation**: Context-aware hardware back button support with animated overlays.

---

## 🧪 Testing

EDVO maintains a comprehensive test suite covering:

*   **Unit Tests**: Crypto operations, session management, password generation
*   **Integration Tests**: Repository patterns, persistence, authentication flows
*   **System Tests**: End-to-end user flows (Fresh Start, Daily Drive, Panic Mode, Disaster Recovery)

Run tests:
```shell
./gradlew :composeApp:testDebugUnitTest
```

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
3.  Write tests for new features.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.