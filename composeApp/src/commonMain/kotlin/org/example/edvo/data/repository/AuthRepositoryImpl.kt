package org.example.edvo.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.example.edvo.core.crypto.CryptoManager
import org.example.edvo.core.session.SessionManager
import org.example.edvo.db.EdvoDatabase
import org.example.edvo.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val database: EdvoDatabase,
    private val driverFactory: org.example.edvo.db.DatabaseDriverFactory? = null
) : AuthRepository {

    private val queries = database.appConfigQueries

    // ... (Companion object omitted for brevity, assuming standard replace works on constructor and method)
    // Wait, replace_file_content replaces chunks. I should target constructor and wipeData separately.
    // I will use multi_replace for safer editing.
    // Actually, I can use replace_file_content for constructor block if I include enough context.
    
    // Continuing with replace of Constructor:
    /* Using Multi-Replace in next step */

    companion object {
        private const val KEY_SALT = "master_salt"
        // In a real app we might store a hash of the key, but to be strictly zero knowledge 
        // we can encrypt a known token effectively acting as a Key Check Value (KCV).
        private const val KEY_VALIDATION_TOKEN = "validation_token"
        // Can also store the IV for the token if needed, or simply prepend it.
        // For simplicity, let's assume CryptoManager.encrypt output includes IV? 
        // No, current CryptoManager returns raw ciphertext and expects separate IV.
        // So we need to store IV for the validation token too.
        private const val KEY_VALIDATION_IV = "validation_iv"
        
        private const val KNOWN_VALUE = "VALID_USER"
        
        // Feature Flags
        const val KEY_FEATURE_SCREENSHOTS = "attr_screenshots"
        const val KEY_FEATURE_COPY_PASTE = "attr_copy_paste"
    }

    override suspend fun isUserRegistered(): Boolean {
        return withContext(Dispatchers.IO) {
            queries.selectByKey(KEY_SALT).executeAsOneOrNull() != null
        }
    }

    override suspend fun register(password: String) {
        withContext(Dispatchers.IO) {
            val salt = CryptoManager.generateRandomBytes(32)
            val masterKey = CryptoManager.deriveKey(password.toCharArray(), salt)
            
            // Generate validation token
            val iv = CryptoManager.generateRandomBytes(12)
            val tokenEncrypted = CryptoManager.encrypt(KNOWN_VALUE.encodeToByteArray(), masterKey, iv)
            
            database.transaction {
                queries.insertOrReplace(KEY_SALT, salt)
                queries.insertOrReplace(KEY_VALIDATION_TOKEN, tokenEncrypted)
                queries.insertOrReplace(KEY_VALIDATION_IV, iv)
            }
            
            // Auto-login
            SessionManager.startSession(masterKey)
        }
    }

    override suspend fun login(password: String): Boolean {
        return withContext(Dispatchers.IO) {
            val salt = queries.selectByKey(KEY_SALT).executeAsOneOrNull() ?: return@withContext false
            val tokenEncrypted = queries.selectByKey(KEY_VALIDATION_TOKEN).executeAsOneOrNull() ?: return@withContext false
            val iv = queries.selectByKey(KEY_VALIDATION_IV).executeAsOneOrNull() ?: return@withContext false
            
            val derivedKey = CryptoManager.deriveKey(password.toCharArray(), salt)
            
            val decrypted = CryptoManager.decrypt(tokenEncrypted, derivedKey, iv)
            if (decrypted != null && decrypted.decodeToString() == KNOWN_VALUE) {
                SessionManager.startSession(derivedKey)
                true
            } else {
                false
            }
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        withContext(Dispatchers.IO) {
            // 1. Authenticate Old
            val salt = queries.selectByKey(KEY_SALT).executeAsOneOrNull() ?: throw IllegalStateException("No user registered")
            val tokenEncrypted = queries.selectByKey(KEY_VALIDATION_TOKEN).executeAsOneOrNull() ?: throw IllegalStateException("Corrupted auth data")
            val tokenIv = queries.selectByKey(KEY_VALIDATION_IV).executeAsOneOrNull() ?: throw IllegalStateException("Corrupted auth data")
            
            val oldKey = CryptoManager.deriveKey(oldPassword.toCharArray(), salt)
            val decryptedCheck = CryptoManager.decrypt(tokenEncrypted, oldKey, tokenIv)
            
            if (decryptedCheck == null || decryptedCheck.decodeToString() != KNOWN_VALUE) {
                throw IllegalStateException("Invalid current password")
            }
            
            // 2. Snapshot & Decrypt All Data
            // We need to access NoteQueries. How? 
            // We need to inject the full database, not just appConfigQueries. 
            // We can access `database.noteQueries` directly.
            
            val noteQueries = database.noteQueries
            val allNotes = noteQueries.selectAllFull().executeAsList()
            
            // Decrypt to memory
            val decryptedNotes = allNotes.map { noteEntity ->
                // Assumption: SQLDelight generates properties as camelCase
                val plainContent = CryptoManager.decrypt(noteEntity.content_encrypted, oldKey, noteEntity.iv) 
                    ?: throw IllegalStateException("Failed to decrypt note ${noteEntity.id} with valid old key. Data corruption?")
                Triple(noteEntity, plainContent, noteEntity.updated_at)
            }
            
            // 3. Re-Key
            val newSalt = CryptoManager.generateRandomBytes(32)
            val newKey = CryptoManager.deriveKey(newPassword.toCharArray(), newSalt)
            
            // New Validation Token
            val newKvIv = CryptoManager.generateRandomBytes(12)
            val newTokenEncrypted = CryptoManager.encrypt(KNOWN_VALUE.encodeToByteArray(), newKey, newKvIv)
            
            // 4. Re-Encrypt All Data
            val reEncryptedNotes = decryptedNotes.map { (entity, plainBytes, updatedAt) ->
                val newIv = CryptoManager.generateRandomBytes(12)
                val newCipher = CryptoManager.encrypt(plainBytes, newKey, newIv)
                // Use Triple(id, newCipher, newIv)
                Triple(entity.id, newCipher, newIv)
            }
            
            // 5. Atomic Commit
            database.transaction {
                // Update Auth Config
                queries.insertOrReplace(KEY_SALT, newSalt)
                queries.insertOrReplace(KEY_VALIDATION_TOKEN, newTokenEncrypted)
                queries.insertOrReplace(KEY_VALIDATION_IV, newKvIv)
                
                // Update Notes
                reEncryptedNotes.forEachIndexed { index, (id, newCipher, newIv) ->
                    val originalEntity = decryptedNotes[index].first
                    noteQueries.insertNote(
                        id = id,
                        title = originalEntity.title,
                        content_encrypted = newCipher,
                        iv = newIv,
                        created_at = originalEntity.created_at,
                        updated_at = originalEntity.updated_at 
                    )
                }
            }
            
            // 6. Cleanup
            SessionManager.clearSession()
        }
    }

    override suspend fun wipeData() {
        withContext(Dispatchers.IO) {
            // "Nuclear Wipe" via SQL Truncation
            // This avoids file lock issues on Android by keeping the connection open
            // but emptying all content.
            try {
                database.transaction {
                    database.noteQueries.deleteAll()
                    queries.deleteAll()
                }
            } catch (e: Exception) {
                println("WipeData: SQL Truncate failed: ${e.message}")
                throw e
            }
            
            SessionManager.clearSession()
        }
    }

    override suspend fun verifyPassword(password: String): Boolean {
        // Same logic as login but without starting session
        return withContext(Dispatchers.IO) {
            val salt = queries.selectByKey(KEY_SALT).executeAsOneOrNull() ?: return@withContext false
            val tokenEncrypted = queries.selectByKey(KEY_VALIDATION_TOKEN).executeAsOneOrNull() ?: return@withContext false
            val iv = queries.selectByKey(KEY_VALIDATION_IV).executeAsOneOrNull() ?: return@withContext false
            
            val derivedKey = CryptoManager.deriveKey(password.toCharArray(), salt)
            
            val decrypted = CryptoManager.decrypt(tokenEncrypted, derivedKey, iv)
            if (decrypted != null && decrypted.decodeToString() == KNOWN_VALUE) {
                true
            } else {
                false
            }
        }
    }

    override suspend fun exportBackup(password: String): ByteArray {
        return withContext(Dispatchers.IO) {
            // 1. Get current key (we assume session is active or we re-derive?
            // "Decrypt them using the Current Session Key".
            val sessionKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Unlock vault first")
            
            // 2. Fetch all notes
            val allNotes = database.noteQueries.selectAllFull().executeAsList()
            
            // 3. Decrypt
            val plainNotes = allNotes.map { entity ->
                val plainContent = CryptoManager.decrypt(entity.content_encrypted, sessionKey, entity.iv)
                    ?.decodeToString() ?: ""
                
                org.example.edvo.core.backup.BackupNote(
                    id = entity.id,
                    title = entity.title,
                    content = plainContent,
                    createdAt = entity.created_at,
                    updatedAt = entity.updated_at
                )
            }
            
            // 4. Create and Encrypt Backup
            org.example.edvo.core.backup.BackupManager.createBackup(password, plainNotes)
        }
    }

    override suspend fun importBackup(password: String, data: ByteArray) {
        withContext(Dispatchers.IO) {
            val sessionKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Unlock vault first")

            // 1. Decrypt Backup
            val notes = org.example.edvo.core.backup.BackupManager.restoreBackup(password, data)
            
            // 2. Insert (Overwrite)
            database.transaction {
                notes.forEach { note ->
                    val iv = CryptoManager.generateRandomBytes(12)
                    val contentEncrypted = CryptoManager.encrypt(note.content.encodeToByteArray(), sessionKey, iv)
                    
                    database.noteQueries.insertNote(
                        id = note.id,
                        title = note.title,
                        content_encrypted = contentEncrypted,
                        iv = iv,
                        created_at = note.createdAt,
                        updated_at = note.updatedAt
                    )
                }
            }
        }
    }


    override suspend fun getFeatureFlag(key: String, defaultValue: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            val bytes = queries.selectByKey(key).executeAsOneOrNull()
            if (bytes != null) {
                // Stored as single byte: 1 = true, 0 = false
                bytes[0] == 1.toByte()
            } else {
                defaultValue
            }
        }
    }

    override suspend fun setFeatureFlag(key: String, enabled: Boolean) {
        withContext(Dispatchers.IO) {
            val bytes = ByteArray(1) { if (enabled) 1 else 0 }
            queries.insertOrReplace(key, bytes)
        }
    }
}
