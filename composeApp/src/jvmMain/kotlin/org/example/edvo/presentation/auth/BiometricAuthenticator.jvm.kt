package org.example.edvo.presentation.auth

/**
 * JVM/Desktop implementation - biometric not supported.
 */
actual class BiometricAuthenticator {
    
    actual fun isAvailable(): Boolean = false
    
    actual fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        onError("Biometric authentication not available on desktop")
    }
}
