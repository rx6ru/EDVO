package org.example.edvo.core.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.AEADBadTagException

actual object CryptoManager {
    private const val ALGORITHM_AES = "AES"
    private const val TRANSFORMATION_GCM = "AES/GCM/NoPadding"
    private const val ALGORITHM_PBKDF2 = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH_BITS = 256
    private const val ITERATION_COUNT = 10000 // OWASP recommended minimum for PBKDF2 is higher, but 10k is acceptable for mobile if performance is key. Let's use 100000 for better security? User didn't specify, sticking to standard safe default. 
    // Actually, user said "Input: User Password + Random 32-byte Master Salt. Output: 256-bit AES Master Key."
    // Let's use 65536 iterations as a good balance.
    private const val ITERATIONS = 65536
    private const val TAG_LENGTH_BITS = 128

    actual fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM_PBKDF2)
        val secret = factory.generateSecret(spec)
        return secret.encoded
    }

    actual fun encrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val keySnapshot = key.clone() // Defensive copy
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION_GCM)
            val secretKeySpec = SecretKeySpec(keySnapshot, ALGORITHM_AES)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec)
            return cipher.doFinal(data)
        } catch (e: java.security.GeneralSecurityException) {
            // Can happen if keySnapshot gets wiped (unlikely as it's local) or invalid params
            throw RuntimeException("Encryption failed", e)
        } finally {
            keySnapshot.fill(0) // Wipe local copy
        }
    }

    actual fun decrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? {
        val keySnapshot = key.clone() // Defensive copy
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION_GCM)
            val secretKeySpec = SecretKeySpec(keySnapshot, ALGORITHM_AES)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec)
            return cipher.doFinal(cipherText)
        } catch (e: AEADBadTagException) {
            return null
        } catch (e: java.security.GeneralSecurityException) {
            // If the key was wiped/corrupted
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            keySnapshot.fill(0) // Wipe local copy
        }
    }

    actual fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}
