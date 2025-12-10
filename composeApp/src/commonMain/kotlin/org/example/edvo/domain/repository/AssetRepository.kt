package org.example.edvo.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.edvo.domain.model.AssetDetail
import org.example.edvo.domain.model.AssetSummary

interface AssetRepository {
    fun getAssets(): Flow<List<AssetSummary>>
    suspend fun getAssetById(id: String): AssetDetail?
    suspend fun saveAsset(id: String? = null, title: String, content: String)
    suspend fun deleteAsset(id: String)
}
