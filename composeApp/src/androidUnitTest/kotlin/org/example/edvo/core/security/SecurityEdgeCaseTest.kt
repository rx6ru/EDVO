package org.example.edvo.core.security

import kotlinx.coroutines.runBlocking
import org.example.edvo.core.crypto.CryptoManager
import org.example.edvo.core.session.SessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Security-focused edge case tests for crypto and session management.
 */
class SecurityEdgeCaseTest {

    @Before
    fun setup() {
        SessionManager.clearSession()
    }

    @After
    fun tearDown() {
        SessionManager.clearSession()
    }

    // ==================== CRYPTO EDGE CASES ====================

    @Test
    fun test_emptyPassword_derivesValidKey() {
        val salt = CryptoManager.generateRandomBytes(32)
        val key = CryptoManager.deriveKey("".toCharArray(), salt)
        assertEquals(32, key.size)
    }

    @Test
    fun test_veryLongPassword_works() {
        val longPassword = "A".repeat(1000)
        val salt = CryptoManager.generateRandomBytes(32)
        val key = CryptoManager.deriveKey(longPassword.toCharArray(), salt)
        assertEquals(32, key.size)
    }

    @Test
    fun test_unicodePassword_works() {
        val unicodePassword = "密码🔐пароль"
        val salt = CryptoManager.generateRandomBytes(32)
        val key = CryptoManager.deriveKey(unicodePassword.toCharArray(), salt)
        assertEquals(32, key.size)
    }

    @Test
    fun test_differentSalts_produceDifferentKeys() {
        val password = "SamePassword"
        val salt1 = CryptoManager.generateRandomBytes(32)
        val salt2 = CryptoManager.generateRandomBytes(32)
        
        val key1 = CryptoManager.deriveKey(password.toCharArray(), salt1)
        val key2 = CryptoManager.deriveKey(password.toCharArray(), salt2)
        
        assertFalse("Different salts should produce different keys", key1.contentEquals(key2))
    }

    @Test
    fun test_sameSalt_producesSameKey() {
        val password = "SamePassword"
        val salt = CryptoManager.generateRandomBytes(32)
        
        val key1 = CryptoManager.deriveKey(password.toCharArray(), salt)
        val key2 = CryptoManager.deriveKey(password.toCharArray(), salt)
        
        assertTrue("Same salt should produce same key", key1.contentEquals(key2))
    }

    @Test
    fun test_encryptEmptyData_works() {
        val key = CryptoManager.generateRandomBytes(32)
        val iv = CryptoManager.generateRandomBytes(12)
        val plaintext = ByteArray(0)
        
        val encrypted = CryptoManager.encrypt(plaintext, key, iv)
        val decrypted = CryptoManager.decrypt(encrypted, key, iv)
        
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun test_encryptLargeData_works() {
        val key = CryptoManager.generateRandomBytes(32)
        val iv = CryptoManager.generateRandomBytes(12)
        val plaintext = ByteArray(1024 * 100) { it.toByte() } // 100KB
        
        val encrypted = CryptoManager.encrypt(plaintext, key, iv)
        val decrypted = CryptoManager.decrypt(encrypted, key, iv)
        
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun test_decryptWithWrongIv_fails() {
        val key = CryptoManager.generateRandomBytes(32)
        val iv1 = CryptoManager.generateRandomBytes(12)
        val iv2 = CryptoManager.generateRandomBytes(12)
        val plaintext = "Secret data".encodeToByteArray()
        
        val encrypted = CryptoManager.encrypt(plaintext, key, iv1)
        val decrypted = CryptoManager.decrypt(encrypted, key, iv2)
        
        assertTrue("Wrong IV should fail decryption", 
            decrypted == null || !plaintext.contentEquals(decrypted))
    }

    @Test
    fun test_tamperedCiphertext_failsDecryption() {
        val key = CryptoManager.generateRandomBytes(32)
        val iv = CryptoManager.generateRandomBytes(12)
        val plaintext = "Secret data".encodeToByteArray()
        
        val encrypted = CryptoManager.encrypt(plaintext, key, iv)
        encrypted[encrypted.size / 2] = (encrypted[encrypted.size / 2].toInt() xor 0xFF).toByte()
        
        val decrypted = CryptoManager.decrypt(encrypted, key, iv)
        assertNull("Tampered ciphertext should fail decryption", decrypted)
    }

    // ==================== SESSION EDGE CASES ====================

    @Test
    fun test_sessionNotActive_beforeStart() {
        assertFalse(SessionManager.isSessionActive())
        assertNull(SessionManager.getMasterKey())
    }

    @Test
    fun test_sessionActive_afterStart() {
        val key = CryptoManager.generateRandomBytes(32)
        SessionManager.startSession(key)
        
        assertTrue(SessionManager.isSessionActive())
        assertNotNull(SessionManager.getMasterKey())
        assertTrue(key.contentEquals(SessionManager.getMasterKey()))
    }

    @Test
    fun test_sessionCleared_afterClear() {
        val key = CryptoManager.generateRandomBytes(32)
        SessionManager.startSession(key)
        assertTrue(SessionManager.isSessionActive())
        
        SessionManager.clearSession()
        
        assertFalse(SessionManager.isSessionActive())
        assertNull(SessionManager.getMasterKey())
    }

    @Test
    fun test_multipleSessionStarts_lastWins() {
        val key1 = CryptoManager.generateRandomBytes(32)
        val key2 = CryptoManager.generateRandomBytes(32)
        
        SessionManager.startSession(key1)
        SessionManager.startSession(key2)
        
        assertTrue(key2.contentEquals(SessionManager.getMasterKey()))
    }

    // ==================== RANDOM GENERATION TESTS ====================

    @Test
    fun test_randomBytes_uniqueEachCall() {
        val random1 = CryptoManager.generateRandomBytes(32)
        val random2 = CryptoManager.generateRandomBytes(32)
        
        assertFalse("Random bytes should be unique", random1.contentEquals(random2))
    }

    @Test
    fun test_randomBytes_correctLength() {
        listOf(1, 12, 16, 32, 64).forEach { length ->
            val bytes = CryptoManager.generateRandomBytes(length)
            assertEquals("Random bytes should be $length bytes", length, bytes.size)
        }
    }
}
