package org.example.edvo.presentation.auth

/**
 * Platform abstraction for biometric authentication.
 * Android: Shows BiometricPrompt
 * Other platforms: No-op (biometric not supported)
 */
expect class BiometricAuthenticator() {
    /**
     * Check if biometric authentication is available on this device.
     */
    fun isAvailable(): Boolean
    
    /**
     * Authenticate using biometrics.
     * @param onSuccess Called when authentication succeeds
     * @param onError Called when authentication fails with error message
     * @param onCancel Called when user cancels authentication
     */
    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    )
}
