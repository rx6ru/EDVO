package org.example.edvo.core.crypto

import org.example.edvo.core.session.SessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import java.util.Arrays
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class CryptoTest {

    private val keysCleanup = mutableListOf<ByteArray>()

    @After
    fun tearDown() {
        // Hygiene: Wipe all keys used in tests
        keysCleanup.forEach { it.fill(0) }
        keysCleanup.clear()
        SessionManager.clearSession()
    }

    private fun registerForCleanup(bytes: ByteArray): ByteArray {
        keysCleanup.add(bytes)
        return bytes
    }

    @Test
    fun testEncryptDecrypt() {
        // Setup
        val password = "StrongPassword123".toCharArray()
        val salt = CryptoManager.generateRandomBytes(32)
        val plainText = "This is a secret message".toByteArray(Charsets.UTF_8)
        
        // Key Derivation
        val key = registerForCleanup(CryptoManager.deriveKey(password, salt))
        assertEquals("Key should be 32 bytes (256 bits)", 32, key.size)
        
        // Encryption
        val iv = CryptoManager.generateRandomBytes(12)
        val cipherText = CryptoManager.encrypt(plainText, key, iv)
        assertTrue("Cipher text should not be empty", cipherText.isNotEmpty())
        assertFalse("Cipher text should not equal plain text", Arrays.equals(plainText, cipherText))
        
        // Decryption
        val decrypted = CryptoManager.decrypt(cipherText, key, iv)
        assertNotNull("Decryption should succeed", decrypted)
        assertArrayEquals("Decrypted text should match original", plainText, decrypted)
    }

    @Test
    fun testDecryptWrongKey() {
        val password = "CorrectPassword".toCharArray()
        val wrongPassword = "WrongPassword".toCharArray()
        val salt = CryptoManager.generateRandomBytes(32)
        
        val key = registerForCleanup(CryptoManager.deriveKey(password, salt))
        val wrongKey = registerForCleanup(CryptoManager.deriveKey(wrongPassword, salt))
        
        val plainText = "Secret Data".toByteArray()
        val iv = CryptoManager.generateRandomBytes(12)
        
        val cipherText = CryptoManager.encrypt(plainText, key, iv)
        
        val decrypted = CryptoManager.decrypt(cipherText, wrongKey, iv)
        assertNull("Decryption with wrong key should return null (AEAD failure)", decrypted)
    }

    @Test
    fun testDecryptTamperedData() {
        val password = "Password".toCharArray()
        val salt = CryptoManager.generateRandomBytes(32)
        val key = registerForCleanup(CryptoManager.deriveKey(password, salt))
        val plainText = "Data".toByteArray()
        val iv = CryptoManager.generateRandomBytes(12)
        
        val cipherText = CryptoManager.encrypt(plainText, key, iv)
        
        // Tamper with the last byte
        cipherText[cipherText.lastIndex] = (cipherText.last() + 1).toByte()
        
        val decrypted = CryptoManager.decrypt(cipherText, key, iv)
        assertNull("Decryption of tampered data should return null", decrypted)
    }

    @Test
    fun testDeriveKeySaltVariance() {
        val password = "SamePassword".toCharArray()
        val salt1 = CryptoManager.generateRandomBytes(32)
        val salt2 = CryptoManager.generateRandomBytes(32)
        
        val key1 = registerForCleanup(CryptoManager.deriveKey(password, salt1))
        val key2 = registerForCleanup(CryptoManager.deriveKey(password, salt2))
        
        assertFalse("Keys with different salts must be different", Arrays.equals(key1, key2))
    }

    @Test
    fun testClearSession_State() {
        val key = registerForCleanup(CryptoManager.generateRandomBytes(32))
        SessionManager.startSession(key)
        
        assertTrue("Session should be active", SessionManager.isSessionActive())
        assertArrayEquals("Session key should match", key, SessionManager.getMasterKey())
        
        SessionManager.clearSession()
        
        assertFalse("Session should be inactive", SessionManager.isSessionActive())
        assertNull("Master key should be null", SessionManager.getMasterKey())
        
        // Verify key data is wiped (check original reference)
        val allZeros = ByteArray(32) { 0 }
        assertArrayEquals("Original key array should be zero-filled", allZeros, key)
    }

    @Test
    fun testConcurrency_RaceCondition() {
        val key = registerForCleanup(CryptoManager.generateRandomBytes(32))
        val iv = CryptoManager.generateRandomBytes(12)
        val data = "Secret".toByteArray()
        
        val running = AtomicBoolean(true)
        val latch = CountDownLatch(1)
        val errors = mutableListOf<Throwable>()
        
        // Background thread constantly trying to encrypt
        val thread = Thread {
            try {
                while (running.get()) {
                    val currentKey = SessionManager.getMasterKey()
                    if (currentKey != null) {
                        try {
                            // If key is wiped mid-read, this should fail gracefully with a SecurityException
                            CryptoManager.encrypt(data, currentKey, iv)
                        } catch (e: java.security.GeneralSecurityException) {
                            // Expected: Key was likely wiped (filled with 0s) during operation
                            // resulting in invalid AES key or tag issues.
                        } catch (e: NullPointerException) {
                            errors.add(e) // Logic error: AtomicReference failed to protect nullability
                        } catch (e: ConcurrentModificationException) {
                            errors.add(e) // Logic error: Structure unsafe
                        } catch (e: IndexOutOfBoundsException) {
                            errors.add(e) // Logic error: Array access unsafe
                        } catch (e: Throwable) {
                            // Catch any other unexpected crashes/Exceptions
                             if (e !is java.security.GeneralSecurityException) {
                                errors.add(e)
                             }
                        }
                    }
                    // Yield to increase race probability
                    if (Thread.interrupted()) break
                }
            } catch (e: Throwable) {
                errors.add(e)
            } finally {
                latch.countDown()
            }
        }
        
        thread.start()
        
        // Main thread toggles session vigorously
        val iterations = 5000
        for (i in 0 until iterations) {
            // Hygiene: Register clone so duplicate verification in tearDown ensures 0-fill
            // even if SessionManager wipes it (double wipe is safe).
            // Crucial: We must clone because the previous iteration's key is dead (wiped).
            val tempKey = registerForCleanup(key.clone())
            
            SessionManager.startSession(tempKey)
            // Natural contention, no sleep
            if (i % 10 == 0) Thread.yield() 
            
            SessionManager.clearSession()
        }
        
        running.set(false)
        latch.await(5, TimeUnit.SECONDS)
        
        if (errors.isNotEmpty()) {
            fail("Concurrency errors detected: ${errors.first().javaClass.simpleName} - ${errors.first().message}")
        }
        
        assertFalse("Session should be cleared at end", SessionManager.isSessionActive())
    }
}
