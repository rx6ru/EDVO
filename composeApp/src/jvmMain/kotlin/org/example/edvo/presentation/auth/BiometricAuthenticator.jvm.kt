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
    
    actual fun authenticateSecure(
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        onError("Biometric authentication not available on desktop")
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

