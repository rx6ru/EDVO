package org.example.edvo.domain.model

data class AssetSummary(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class AssetDetail(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long
)
