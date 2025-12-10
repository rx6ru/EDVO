package org.example.edvo.core.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.edvo.core.crypto.CryptoManager

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long,
    val assets: List<BackupAsset> = emptyList(), // Primary for new backups
    val notes: List<BackupAsset> = emptyList()   // Legacy support for old backups
)

@Serializable
data class BackupAsset(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

object BackupManager {
    // Header to identify our backup files
    private val HEADER = "EDVO_BACKUP_V1".encodeToByteArray()

    fun createBackup(password: String, assets: List<BackupAsset>): ByteArray {
        val jsonString = Json.encodeToString(BackupData(
            timestamp = 0L, // Use system time in real app
            assets = assets,
            notes = emptyList() // Do not write to legacy key
        ))
        
        // Encrypt JSON
        val salt = CryptoManager.generateRandomBytes(32)
        val key = CryptoManager.deriveKey(password.toCharArray(), salt)
        val iv = CryptoManager.generateRandomBytes(12)
        
        val encryptedJson = CryptoManager.encrypt(jsonString.encodeToByteArray(), key, iv)
        
        return salt + iv + encryptedJson
    }
    
    fun restoreBackup(password: String, data: ByteArray): List<BackupAsset> {
        // Parse format
        // SALT = 32 bytes
        // IV = 12 bytes
        if (data.size < 44) throw IllegalArgumentException("Invalid backup file format")
        
        val salt = data.copyOfRange(0, 32)
        val iv = data.copyOfRange(32, 44)
        val ciphertext = data.copyOfRange(44, data.size)
        
        val key = CryptoManager.deriveKey(password.toCharArray(), salt)
        val decryptedBytes = CryptoManager.decrypt(ciphertext, key, iv) 
            ?: throw IllegalArgumentException("Decryption failed. Wrong password or corrupted file.")
            
        val jsonString = decryptedBytes.decodeToString()
        
        // Relaxed JSON parsing to allow missing keys if needed, though default values handle it.
        val json = Json { ignoreUnknownKeys = true }
        val backupData = json.decodeFromString<BackupData>(jsonString)
        
        // COMPATIBILITY LOGIC:
        // If assets list is populated, use it.
        // If assets is empty but notes is populated, use notes.
        return if (backupData.assets.isNotEmpty()) {
            backupData.assets
        } else {
            backupData.notes
        }
    }
}
