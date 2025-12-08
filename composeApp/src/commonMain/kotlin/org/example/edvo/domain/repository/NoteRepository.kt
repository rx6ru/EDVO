package org.example.edvo.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.edvo.domain.model.NoteDetail
import org.example.edvo.domain.model.NoteSummary

interface NoteRepository {
    fun getNotes(): Flow<List<NoteSummary>>
    suspend fun getNoteById(id: String): NoteDetail?
    suspend fun saveNote(id: String? = null, title: String, content: String)
    suspend fun deleteNote(id: String)
}
