package org.example.edvo.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.example.edvo.domain.model.NoteDetail
import org.example.edvo.domain.model.NoteSummary
import org.example.edvo.domain.repository.NoteRepository

sealed class NoteListState {
    object Loading : NoteListState()
    data class Success(val notes: List<NoteSummary>) : NoteListState()
    data class Error(val message: String) : NoteListState()
}

class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<NoteListState>(NoteListState.Loading)
    val listState = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<NoteDetail?>(null)
    val detailState = _detailState.asStateFlow()
    
    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            try {
                combine(
                    repository.getNotes(),
                    _searchQuery
                ) { notes, query ->
                    if (query.isBlank()) {
                        notes
                    } else {
                        // Fuzzy-ish: Case insensitive contains.
                        // Can be improved to subsequence if needed.
                        notes.filter { it.title.contains(query, ignoreCase = true) }
                    }
                }.collectLatest { filtered ->
                    _listState.value = NoteListState.Success(filtered)
                }
            } catch (e: Exception) {
                _listState.value = NoteListState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadNoteDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = null // Reset
            val note = repository.getNoteById(id)
            _detailState.value = note
        }
    }
    
    fun clearDetail() {
        _detailState.value = null
    }

    fun saveNote(id: String?, title: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(id, title, content)
            // List will auto-update via Flow
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
            // If deleting current detail, clear it?
            if (_detailState.value?.id == id) {
                _detailState.value = null
            }
        }
    }
}
