package org.example.edvo.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.example.edvo.core.crypto.CryptoManager
import org.example.edvo.core.session.SessionManager
import org.example.edvo.db.EdvoDatabase
import org.example.edvo.domain.model.NoteDetail
import org.example.edvo.domain.model.NoteSummary
import org.example.edvo.domain.repository.NoteRepository
import java.util.UUID

class NoteRepositoryImpl(
    private val database: EdvoDatabase
) : NoteRepository {

    private val queries = database.noteQueries

    override fun getNotes(): Flow<List<NoteSummary>> {
        return queries.selectAll().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { entity ->
                // Lazy: Do NOT decrypt content here.
                NoteSummary(
                    id = entity.id,
                    title = entity.title,
                    createdAt = entity.created_at,
                    updatedAt = entity.updated_at
                )
            }
        }
    }

    override suspend fun getNoteById(id: String): NoteDetail? {
        return withContext(Dispatchers.IO) {
            val entity = queries.getById(id).executeAsOneOrNull() ?: return@withContext null
            val masterKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Session locked")

            try {
                // Decrypt content
                val decryptedBytes = CryptoManager.decrypt(entity.content_encrypted, masterKey, entity.iv)
                val content = decryptedBytes?.decodeToString() ?: "Error: Decryption Failed"
                
                NoteDetail(
                    id = entity.id,
                    title = entity.title,
                    content = content,
                    updatedAt = entity.updated_at
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // Return detail with error message in content or null?
                // Returning object allows user to allow seeing title and maybe deleting.
                NoteDetail(
                    id = entity.id,
                    title = entity.title,
                    content = "Error: ${e.message}",
                    updatedAt = entity.updated_at
                )
            }
        }
    }

    override suspend fun saveNote(id: String?, title: String, content: String) {
        withContext(Dispatchers.IO) {
            val masterKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Session locked")
            
            val noteId = id ?: UUID.randomUUID().toString()
            val iv = CryptoManager.generateRandomBytes(12)
            val encryptedContent = CryptoManager.encrypt(content.toByteArray(), masterKey, iv)
            val now = System.currentTimeMillis()
            
            // Should preserve created_at if updating? 
            // Query replaces row. We need to fetch original created_at if exists?
            // Or just simplified for now: upsert.
            // If ID exists, we technically overwrite created_at if we pass new time.
            // Let's check if it exists or use insert logic.
            // Our SQL is INSERT OR REPLACE.
            // If we want to preserve created_at, we should fetch it first or separate update query.
            // For MVP: Set created_at = updated_at = now if new. 
            // If existing, we ideally want to keep old created_at.
            
            val originalCreatedAt = if (id != null) {
                queries.getById(id).executeAsOneOrNull()?.created_at ?: now
            } else {
                now
            }

            queries.insertNote(
                id = noteId,
                title = title,
                content_encrypted = encryptedContent,
                iv = iv,
                created_at = originalCreatedAt,
                updated_at = now
            )
        }
    }

    override suspend fun deleteNote(id: String) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }
}
