package org.example.edvo.core.crypto

expect object CryptoManager {
    /**
     * Derives a 32-byte (256-bit) AES key from the password and salt using PBKDF2.
     * @param password The user's password.
     * @param salt A random salt (recommended 32 bytes).
     * @return The derived key as a ByteArray.
     */
    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray

    /**
     * Encrypts data using AES-256-GCM.
     * @param data The plaintext data.
     * @param key The 32-byte AES key.
     * @param iv The 12-byte initialization vector.
     * @return The ciphertext (which includes the authentication tag).
     */
    fun encrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray

    /**
     * Decrypts data using AES-256-GCM.
     * @param cipherText The ciphertext.
     * @param key The 32-byte AES key.
     * @param iv The 12-byte initialization vector.
     * @return The plaintext data, or null if decryption fails (e.g., bad tag).
     */
    fun decrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray?

    /**
     * Generates a random byte array of the specified size.
     * @param size The number of bytes to generate.
     */
    fun generateRandomBytes(size: Int): ByteArray
}
