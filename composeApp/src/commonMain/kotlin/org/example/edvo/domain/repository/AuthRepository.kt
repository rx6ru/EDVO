package org.example.edvo.domain.repository

interface AuthRepository {
    suspend fun isUserRegistered(): Boolean
    suspend fun register(password: String)
    suspend fun login(password: String): Boolean
    suspend fun changePassword(oldPassword: String, newPassword: String)
    suspend fun wipeData()
    suspend fun verifyPassword(password: String): Boolean
    suspend fun exportBackup(password: String): ByteArray
    suspend fun importBackup(password: String, data: ByteArray)

    // Feature Flags (Settings)
    suspend fun getFeatureFlag(key: String, defaultValue: Boolean): Boolean
    suspend fun setFeatureFlag(key: String, enabled: Boolean)
}
