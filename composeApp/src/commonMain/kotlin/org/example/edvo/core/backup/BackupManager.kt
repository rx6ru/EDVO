package org.example.edvo.core.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.edvo.core.crypto.CryptoManager

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long,
    val notes: List<BackupNote>
)

@Serializable
data class BackupNote(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

object BackupManager {
    // Header to identify our backup files
    private val HEADER = "EDVO_BACKUP_V1".encodeToByteArray()

    fun createBackup(password: String, notes: List<BackupNote>): ByteArray {
        val jsonString = Json.encodeToString(BackupData(
            timestamp = 0L, // Use system time in real app, stick to 0 or valid long here
            notes = notes
        ))
        
        // Encrypt JSON
        val salt = CryptoManager.generateRandomBytes(32)
        val key = CryptoManager.deriveKey(password.toCharArray(), salt)
        val iv = CryptoManager.generateRandomBytes(12)
        
        val encryptedJson = CryptoManager.encrypt(jsonString.encodeToByteArray(), key, iv)
        
        // Format: [SALT(32)][IV(12)][CIPHERTEXT]
        // Actually prompt requested: 
        // "Prepend the Salt and IV to the file."
        
        return salt + iv + encryptedJson
    }
    
    fun restoreBackup(password: String, data: ByteArray): List<BackupNote> {
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
        val backupData = Json.decodeFromString<BackupData>(jsonString)
        
        return backupData.notes
    }
}
