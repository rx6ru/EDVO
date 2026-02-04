package org.example.edvo.core.biometric

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages biometric-protected key storage using Android Keystore.
 * 
 * Security Model:
 * - Master KEY (not hash) is encrypted with a Keystore-backed AES key
 * - Keystore key requires biometric authentication to decrypt
 * - If biometric enrollment changes, key is invalidated (security measure)
 */
class BiometricKeyManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_ALIAS = "edvo_biometric_wrapper_key"
        private const val PREFS_NAME = "edvo_biometric_prefs"
        private const val KEY_WRAPPED_MASTER = "wrapped_master_key"
        private const val KEY_WRAPPED_IV = "wrapped_iv"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val GCM_TAG_LENGTH = 128
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if device supports biometric authentication.
     * @return BiometricManager.BIOMETRIC_SUCCESS if available, error code otherwise.
     */
    fun canUseBiometrics(): Int {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    }

    /**
     * Check if biometric hardware is available and enrolled.
     */
    fun isBiometricAvailable(): Boolean {
        return canUseBiometrics() == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Check if we have stored biometric credentials.
     */
    fun hasBiometricCredentials(): Boolean {
        return prefs.contains(KEY_WRAPPED_MASTER) && prefs.contains(KEY_WRAPPED_IV)
    }

    /**
     * Store the master key encrypted with a biometric-protected Keystore key.
     * Call this when user enables biometric unlock (after password verification).
     */
    fun storeMasterKeyForBiometrics(masterKey: ByteArray): Boolean {
        return try {
            // Generate or get the Keystore wrapping key
            val secretKey = getOrCreateKeystoreKey()
            
            // Encrypt the master key
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedKey = cipher.doFinal(masterKey)
            val iv = cipher.iv
            
            // Store encrypted key and IV
            prefs.edit()
                .putString(KEY_WRAPPED_MASTER, Base64.encodeToString(encryptedKey, Base64.NO_WRAP))
                .putString(KEY_WRAPPED_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .apply()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get a Cipher configured for decryption, ready for BiometricPrompt.
     * BiometricPrompt will authenticate and unlock the Keystore key.
     */
    fun getCryptoObjectForDecryption(): BiometricPrompt.CryptoObject? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
                ?: return null
            
            val ivString = prefs.getString(KEY_WRAPPED_IV, null) ?: return null
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            
            BiometricPrompt.CryptoObject(cipher)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decrypt the stored master key after successful biometric authentication.
     * @param cipher The authenticated Cipher from BiometricPrompt callback.
     */
    fun decryptMasterKey(cipher: Cipher): ByteArray? {
        return try {
            val encryptedKeyString = prefs.getString(KEY_WRAPPED_MASTER, null) ?: return null
            val encryptedKey = Base64.decode(encryptedKeyString, Base64.NO_WRAP)
            
            cipher.doFinal(encryptedKey)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Clear all biometric data (when user disables biometric unlock).
     */
    fun clearBiometricData() {
        try {
            // Remove stored encrypted key
            prefs.edit()
                .remove(KEY_WRAPPED_MASTER)
                .remove(KEY_WRAPPED_IV)
                .apply()
            
            // Delete Keystore key
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                keyStore.deleteEntry(KEYSTORE_ALIAS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get or create the Keystore key used to wrap the master key.
     */
    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        // Check if key already exists
        val existingKey = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }
        
        // Generate new key with biometric requirement
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .setInvalidatedByBiometricEnrollment(true) // Invalidate if new fingerprint added
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
