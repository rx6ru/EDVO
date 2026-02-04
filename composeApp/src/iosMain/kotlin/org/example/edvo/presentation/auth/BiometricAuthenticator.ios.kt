package org.example.edvo.presentation.auth

/**
 * iOS implementation - biometric not supported yet.
 * Could be implemented with LocalAuthentication framework.
 */
actual class BiometricAuthenticator {
    
    actual fun isAvailable(): Boolean = false
    
    actual fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        onError("Biometric authentication not yet implemented on iOS")
    }
}
