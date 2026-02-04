package org.example.edvo.presentation.auth

import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.example.edvo.AndroidActivityTracker
import org.example.edvo.core.biometric.BiometricKeyManager
import javax.crypto.Cipher

/**
 * Helper class to manage BiometricPrompt lifecycle and callbacks.
 */
class BiometricPromptHelper(
    private val activity: FragmentActivity,
    private val keyManager: BiometricKeyManager,
    private val onSuccess: (masterKey: ByteArray) -> Unit,
    private val onError: (message: String) -> Unit,
    private val onCancel: () -> Unit
) {
    private var biometricPrompt: BiometricPrompt? = null

    fun authenticate() {
        val cryptoObject = keyManager.getCryptoObjectForDecryption()
        if (cryptoObject == null) {
            onError("Biometric credentials not found. Please re-enable biometric unlock.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                
                val cipher = result.cryptoObject?.cipher
                if (cipher == null) {
                    onError("Authentication failed: No cipher available")
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
                    else -> onError(errString.toString())
                }
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onError - the system will show "Try again" and allow retry
            }
        }
        
        biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock EDVO")
            .setSubtitle("Authenticate to access your vault")
            .setNegativeButtonText("Use Password")
            .setConfirmationRequired(false)
            .build()
        
        biometricPrompt?.authenticate(promptInfo, cryptoObject)
    }
    
    fun cancel() {
        biometricPrompt?.cancelAuthentication()
    }
}

/**
 * Composable helper to remember a BiometricPromptHelper instance.
 */
@Composable
fun rememberBiometricPromptHelper(
    keyManager: BiometricKeyManager,
    onSuccess: (masterKey: ByteArray) -> Unit,
    onError: (message: String) -> Unit,
    onCancel: () -> Unit
): BiometricPromptHelper? {
    val activity = AndroidActivityTracker.currentActivity?.get() as? FragmentActivity
    
    return remember(activity, keyManager) {
        activity?.let {
            BiometricPromptHelper(it, keyManager, onSuccess, onError, onCancel)
        }
    }
}
