package org.example.edvo.core.session

import kotlinx.atomicfu.atomic

/**
 * Singleton to hold the Master Key in RAM.
 * "Zero-Knowledge" principle: Key is never stored on disk.
 */
object SessionManager {
    private val _masterKey = atomic<ByteArray?>(null)

    /**
     * Sets the master key.
     * @param key The 32-byte AES Master Key.
     */
    fun startSession(key: ByteArray) {
        _masterKey.value = key
    }

    /**
     * Checks if a session is active.
     */
    fun isSessionActive(): Boolean {
        return _masterKey.value != null
    }

    /**
     * Returns the master key if active, else null.
     */
    fun getMasterKey(): ByteArray? {
        return _masterKey.value
    }

    /**
     * Wipes the key from memory and clears the reference.
     * Uses atomic getAndSet(null) to ensure no new readers get the key,
     * then wipes the reclaimed key data.
     */
    fun clearSession() {
        val oldKey = _masterKey.getAndSet(null)
        oldKey?.fill(0)
    }
}
