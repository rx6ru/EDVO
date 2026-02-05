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
    
    actual fun authenticateSecure(
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        onError("Biometric authentication not yet implemented on iOS")
    }
    
    actual fun hasStoredCredentials(): Boolean = false
    
    actual fun storeMasterKey(masterKey: ByteArray): Boolean = false
    
    actual fun enableBiometric(
        masterKey: ByteArray,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        onError("Not implemented")
    }
    
    actual fun clearCredentials() {}
}

