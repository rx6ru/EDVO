package org.example.edvo.core.crypto

actual object CryptoManager {
    actual fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        throw NotImplementedError("iOS implementation is stubbed on Windows dev environment")
    }

    actual fun encrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        throw NotImplementedError("iOS implementation is stubbed on Windows dev environment")
    }

    actual fun decrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray? {
        throw NotImplementedError("iOS implementation is stubbed on Windows dev environment")
    }

    actual fun generateRandomBytes(size: Int): ByteArray {
        throw NotImplementedError("iOS implementation is stubbed on Windows dev environment")
    }
}
