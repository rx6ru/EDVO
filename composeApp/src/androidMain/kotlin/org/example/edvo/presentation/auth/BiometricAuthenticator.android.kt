package org.example.edvo.presentation.auth

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.example.edvo.AndroidActivityTracker
import org.example.edvo.core.biometric.BiometricKeyManager

actual class BiometricAuthenticator {
    
    actual fun isAvailable(): Boolean {
        val activity = AndroidActivityTracker.currentActivity?.get() ?: return false
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Legacy authenticate - kept for backward compatibility but should not be used.
     */
    actual fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        // Delegate to secure version, but this won't have the key
        authenticateSecure(
            onSuccess = { _ -> onSuccess() },
            onError = onError,
            onCancel = onCancel
        )
    }
    
    /**
     * Authenticate using CryptoObject to securely retrieve the master key.
     * This is the proper way to do biometric auth for EDVO.
     * 
     * @param onSuccess Called with decrypted master key on success
     * @param onError Called when authentication fails
     * @param onCancel Called when user cancels
     */
    actual fun authenticateSecure(
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val activity = AndroidActivityTracker.currentActivity?.get() as? FragmentActivity
        if (activity == null) {
            onError("Cannot access activity for biometric prompt")
            return
        }
        
        val keyManager = BiometricKeyManager(activity)
        
        // Check if we have stored credentials
        if (!keyManager.hasBiometricCredentials()) {
            onError("No biometric credentials stored. Login with password first.")
            return
        }
        
        // Get CryptoObject for decryption
        val cryptoObject = keyManager.getCryptoObjectForDecryption()
        if (cryptoObject == null) {
            onError("Failed to initialize biometric crypto. Try logging in with password.")
            return
        }
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                
                // Decrypt the master key using the authenticated cipher
                val cipher = result.cryptoObject?.cipher
                if (cipher == null) {
                    onError("Biometric auth succeeded but cipher not available")
                    return
                }
                
                val masterKey = keyManager.decryptMasterKey(cipher)
                if (masterKey == null) {
                    onError("Failed to decrypt master key")
                    return
                }
                
                onSuccess(masterKey)
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> onCancel()
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> onError("Too many attempts. Try password.")
                    else -> onError(errString.toString())
                }
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onError - system shows "Try again"
            }
        }
        
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock EDVO")
            .setSubtitle("Use your fingerprint to unlock")
            .setNegativeButtonText("Use Password")
            .setConfirmationRequired(false)
            .build()
        
        // Authenticate WITH the CryptoObject - this is the key difference
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }
    
    /**
     * Check if biometric credentials are stored (master key is available for biometric unlock).
     */
    actual fun hasStoredCredentials(): Boolean {
        val activity = AndroidActivityTracker.currentActivity?.get() ?: return false
        val keyManager = BiometricKeyManager(activity)
        return keyManager.hasBiometricCredentials()
    }
    
    /**
     * Store master key for biometric unlock.
     * Call this after successful password login when biometric is enabled.
     */
    actual fun storeMasterKey(masterKey: ByteArray): Boolean {
        val activity = AndroidActivityTracker.currentActivity?.get() ?: return false
        val keyManager = BiometricKeyManager(activity)
        return keyManager.storeMasterKeyForBiometrics(masterKey)
    }

    /**
     * Enable biometric unlock by encrypting the master key with user authentication.
     */
    actual fun enableBiometric(
        masterKey: ByteArray,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val activity = AndroidActivityTracker.currentActivity?.get() as? FragmentActivity
        if (activity == null) {
            onError("Cannot access activity")
            return
        }
        
        val keyManager = BiometricKeyManager(activity)
        val cryptoObject = keyManager.getCryptoObjectForEncryption()
        
        if (cryptoObject == null) {
            onError("Failed to initialize biometric encryption")
            return
        }
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                
                val cipher = result.cryptoObject?.cipher
                if (cipher == null) {
                    onError("Encryption failed: cipher not available")
                    return
                }
                
                if (keyManager.encryptMasterKey(cipher, masterKey)) {
                    onSuccess()
                } else {
                    onError("Failed to encrypt and store key")
                }
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> onCancel()
                    else -> onError(errString.toString())
                }
            }
        }
        
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Unlock")
            .setSubtitle("Scan fingerprint to confirm")
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false)
            .build()
            
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }
    
    /**
     * Clear stored biometric credentials.
     * Call this when user disables biometric unlock.
     */
    actual fun clearCredentials() {
        val activity = AndroidActivityTracker.currentActivity?.get() ?: return
        val keyManager = BiometricKeyManager(activity)
        keyManager.clearBiometricData()
    }
}

