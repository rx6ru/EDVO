# iOS App Module

This folder contains the native iOS entry point for EDVO.

## Integration

The iOS application consumes the shared logic and UI from the `composeApp` module via the `ComposeApp` framework (compiled from Kotlin).

*   `iOSApp.swift`: Main entry point.
*   `ContentView.swift`: Wraps the shared Compose `MainViewController`.

## Development

Open `iosApp.xcodeproj` in Xcode to build and run the iOS target.
