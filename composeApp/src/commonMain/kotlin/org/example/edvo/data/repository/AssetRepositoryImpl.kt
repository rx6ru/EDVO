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
import org.example.edvo.domain.model.AssetDetail
import org.example.edvo.domain.model.AssetSummary
import org.example.edvo.domain.repository.AssetRepository
import java.util.UUID

class AssetRepositoryImpl(
    private val database: EdvoDatabase
) : AssetRepository {

    private val queries = database.assetQueries

    override fun getAssets(): Flow<List<AssetSummary>> {
        return queries.selectAll().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { entity ->
                // Lazy: Do NOT decrypt content here.
                AssetSummary(
                    id = entity.id,
                    title = entity.title,
                    createdAt = entity.created_at,
                    updatedAt = entity.updated_at
                )
            }
        }
    }

    override suspend fun getAssetById(id: String): AssetDetail? {
        return withContext(Dispatchers.IO) {
            val entity = queries.getById(id).executeAsOneOrNull() ?: return@withContext null
            val masterKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Session locked")

            try {
                // Decrypt content
                val decryptedBytes = CryptoManager.decrypt(entity.content_encrypted, masterKey, entity.iv)
                val content = decryptedBytes?.decodeToString() ?: "Error: Decryption Failed"
                
                AssetDetail(
                    id = entity.id,
                    title = entity.title,
                    content = content,
                    updatedAt = entity.updated_at
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // Return detail with error message in content or null?
                // Returning object allows user to allow seeing title and maybe deleting.
                AssetDetail(
                    id = entity.id,
                    title = entity.title,
                    content = "Error: ${e.message}",
                    updatedAt = entity.updated_at
                )
            }
        }
    }

    override suspend fun saveAsset(id: String?, title: String, content: String) {
        withContext(Dispatchers.IO) {
            val masterKey = SessionManager.getMasterKey() ?: throw IllegalStateException("Session locked")
            
            val assetId = id ?: UUID.randomUUID().toString()
            val iv = CryptoManager.generateRandomBytes(12)
            val encryptedContent = CryptoManager.encrypt(content.toByteArray(), masterKey, iv)
            val now = System.currentTimeMillis()
            
            val originalCreatedAt = if (id != null) {
                queries.getById(id).executeAsOneOrNull()?.created_at ?: now
            } else {
                now
            }

            queries.insertAsset(
                id = assetId,
                title = title,
                content_encrypted = encryptedContent,
                iv = iv,
                created_at = originalCreatedAt,
                updated_at = now
            )
        }
    }

    override suspend fun deleteAsset(id: String) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }
}
