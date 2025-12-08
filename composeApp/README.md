# ComposeApp Module

This is the shared core of the EDVO application, built with **Compose Multiplatform**.

## Structure

*   `src/commonMain`: Shared business logic (ViewModules, Repositories) and UI (Screens, Components).
*   `src/androidMain`: Android-specific implementations (Drivers, Crypto, Resources).
*   `src/iosMain`: iOS-specific implementations (Drivers, Crypto).
*   `src/jvmMain`: Desktop-specific implementations.

## Key Components

*   **NeoTheme**: Custom retro-futuristic design system.
*   **SecureTextField**: Hardened input component with anti-paste/anti-selection logic.
*   **VaultScreen**: Main UI for encrypted note management.
