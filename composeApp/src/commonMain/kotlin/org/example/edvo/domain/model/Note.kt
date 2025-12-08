package org.example.edvo.domain.model

data class NoteSummary(
    val id: String,
    val title: String,
    val updatedAt: Long
)

data class NoteDetail(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long
)
