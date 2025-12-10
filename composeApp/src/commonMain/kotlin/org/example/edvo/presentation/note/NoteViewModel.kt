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
    
    // Search & Sort
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    // Sort State
    private val _sortOption = MutableStateFlow(SortOption.DATE_UPDATED)
    val sortOption = _sortOption.asStateFlow()
    
    private val _sortOrder = MutableStateFlow(SortOrder.DESCENDING)
    val sortOrder = _sortOrder.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            try {
                combine(
                    repository.getNotes(),
                    _searchQuery,
                    _sortOption,
                    _sortOrder
                ) { notes, query, sortOpt, sortOrd ->
                    // 1. Filter
                    val filtered = if (query.isBlank()) {
                        notes
                    } else {
                        notes.filter { it.title.contains(query, ignoreCase = true) }
                    }
                    
                    // 2. Sort
                    val sorted = when (sortOpt) {
                        SortOption.NAME -> filtered.sortedBy { it.title.lowercase() }
                        SortOption.DATE_CREATED -> filtered.sortedBy { it.createdAt }
                        SortOption.DATE_UPDATED -> filtered.sortedBy { it.updatedAt }
                    }
                    
                    if (sortOrd == SortOrder.DESCENDING) {
                        sorted.reversed()
                    } else {
                        sorted
                    }
                }.collectLatest { finalNotes ->
                    _listState.value = NoteListState.Success(finalNotes)
                }
            } catch (e: Exception) {
                _listState.value = NoteListState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    fun onSortChange(option: SortOption) {
        if (_sortOption.value == option) {
            // Toggle order
            _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
        } else {
            // New option, default order (Asc for Name, Desc for Dates?)
            // Prompt says: "clicking them again will switch... represented by up/down arrow"
            // Let's stick to a sensible default.
            _sortOption.value = option
            _sortOrder.value = if (option == SortOption.NAME) SortOrder.ASCENDING else SortOrder.DESCENDING
        }
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

enum class SortOption {
    NAME, DATE_CREATED, DATE_UPDATED
}

enum class SortOrder {
    ASCENDING, DESCENDING
}
