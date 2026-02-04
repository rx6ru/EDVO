package org.example.edvo.presentation.auth

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.example.edvo.AndroidActivityTracker

actual class BiometricAuthenticator {
    
    actual fun isAvailable(): Boolean {
        val activity = AndroidActivityTracker.currentActivity?.get() ?: return false
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    actual fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val activity = AndroidActivityTracker.currentActivity?.get() as? FragmentActivity
        if (activity == null) {
            onError("Cannot access activity for biometric prompt")
            return
        }
        
        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
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
        
        biometricPrompt.authenticate(promptInfo)
    }
}
