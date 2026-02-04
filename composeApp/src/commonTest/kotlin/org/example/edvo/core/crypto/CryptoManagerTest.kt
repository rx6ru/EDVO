package org.example.edvo.core.crypto

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CryptoManagerTest {

    @Test
    fun `test encryption decryption roundtrip`() {
        val originalData = "Hello, World! Secret Data".encodeToByteArray()
        val password = "StrongPassword123".toCharArray()
        val salt = CryptoManager.generateRandomBytes(32)
        val iv = CryptoManager.generateRandomBytes(12)
        
        val key = CryptoManager.deriveKey(password, salt)
        val cipherText = CryptoManager.encrypt(originalData, key, iv)
        
        // Ensure ciphertext is different from plaintext
        assertFalse(originalData.contentEquals(cipherText), "Ciphertext should not match plaintext")
        
        val decryptedData = CryptoManager.decrypt(cipherText, key, iv)
        
        assertNotNull(decryptedData, "Decryption should succeed")
        assertContentEquals(originalData, decryptedData, "Decrypted data should match original")
    }

    @Test
    fun `test decryption with wrong key fails`() {
        val data = "Top Secret".encodeToByteArray()
        val salt = CryptoManager.generateRandomBytes(32)
        val iv = CryptoManager.generateRandomBytes(12)
        
        val correctKey = CryptoManager.deriveKey("CorrectPass".toCharArray(), salt)
        val wrongKey = CryptoManager.deriveKey("WrongPass".toCharArray(), salt)
        
        val cipherText = CryptoManager.encrypt(data, correctKey, iv)
        
        val decrypted = CryptoManager.decrypt(cipherText, wrongKey, iv)
        
        // Current implementation returns null on failure
        assertNull(decrypted, "Decryption with wrong key should fail (return null)")
    }

    @Test
    fun `test decryption with corrupted ciphertext fails`() {
        val data = "Sensitive Info".encodeToByteArray()
        val salt = CryptoManager.generateRandomBytes(32)
        val iv = CryptoManager.generateRandomBytes(12)
        val key = CryptoManager.deriveKey("Pass123".toCharArray(), salt)
        
        val cipherText = CryptoManager.encrypt(data, key, iv)
        
        // Corrupt the last byte (authentication tag or data)
        cipherText[cipherText.lastIndex] = (cipherText.last().toInt() xor 1).toByte()
        
        val decrypted = CryptoManager.decrypt(cipherText, key, iv)
        
        assertNull(decrypted, "Decryption with corrupted ciphertext should fail")
    }

    @Test
    fun `test deriveKey deterministic output`() {
        val password = "ConsistentPass".toCharArray()
        val salt = ByteArray(32) { 1 } // Fixed salt
        
        val key1 = CryptoManager.deriveKey(password, salt)
        val key2 = CryptoManager.deriveKey(password, salt)
        
        assertContentEquals(key1, key2, "PBKDF2 should be deterministic for same input")
    }

    @Test
    fun `test salt uniqueness`() {
        val salt1 = CryptoManager.generateRandomBytes(32)
        val salt2 = CryptoManager.generateRandomBytes(32)
        
        assertFalse(salt1.contentEquals(salt2), "Random salts should be unique")
    }
}
