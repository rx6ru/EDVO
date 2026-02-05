package org.example.edvo.presentation.auth

/**
 * Platform abstraction for biometric authentication.
 * Android: Shows BiometricPrompt with Keystore-backed decryption
 * Other platforms: No-op (biometric not supported)
 */
expect class BiometricAuthenticator() {
    /**
     * Check if biometric authentication is available on this device.
     */
    fun isAvailable(): Boolean
    
    /**
     * Legacy authenticate - kept for backward compatibility.
     * Use authenticateSecure for proper session initialization.
     */
    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    )
    
    /**
     * Authenticate using biometrics and return the decrypted master key.
     * This is the proper way to authenticate - it retrieves the encrypted
     * master key from Keystore and decrypts it using biometric auth.
     * 
     * @param onSuccess Called with decrypted master key on success
     * @param onError Called when authentication fails with error message
     * @param onCancel Called when user cancels authentication
     */
    fun authenticateSecure(
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    )
    
    /**
     * Check if biometric credentials are stored (master key available for biometric unlock).
     */
    fun hasStoredCredentials(): Boolean
    
    /**
     * Store master key for biometric unlock.
     * Call this after successful password login when biometric is enabled.
     */
    fun storeMasterKey(masterKey: ByteArray): Boolean
    
    /**
     * Enable biometric unlock by encrypting the master key with user authentication.
     * This will prompt the user to scan their fingerprint to CONFIRM enabling the feature.
     * 
     * @param masterKey The master key to encrypt and store
     * @param onSuccess Called when key is successfully stored
     * @param onError Called when operation fails
     * @param onCancel Called when user cancels prompt
     */
    fun enableBiometric(
        masterKey: ByteArray,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    )
    
    /**
     * Clear stored biometric credentials.
     */
    fun clearCredentials()
}

