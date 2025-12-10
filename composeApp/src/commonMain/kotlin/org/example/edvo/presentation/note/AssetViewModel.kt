package org.example.edvo.presentation.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.example.edvo.domain.model.AssetDetail
import org.example.edvo.domain.model.AssetSummary
import org.example.edvo.domain.repository.AssetRepository

sealed class AssetListState {
    object Loading : AssetListState()
    data class Success(val assets: List<AssetSummary>) : AssetListState()
    data class Error(val message: String) : AssetListState()
}

class AssetViewModel(
    private val repository: AssetRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<AssetListState>(AssetListState.Loading)
    val listState = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<AssetDetail?>(null)
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
        loadAssets()
    }

    private fun loadAssets() {
        viewModelScope.launch {
            try {
                combine(
                    repository.getAssets(),
                    _searchQuery,
                    _sortOption,
                    _sortOrder
                ) { assets, query, sortOpt, sortOrd ->
                    // 1. Filter
                    val filtered = if (query.isBlank()) {
                        assets
                    } else {
                        assets.filter { it.title.contains(query, ignoreCase = true) }
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
                }.collectLatest { finalAssets ->
                    _listState.value = AssetListState.Success(finalAssets)
                }
            } catch (e: Exception) {
                _listState.value = AssetListState.Error(e.message ?: "Unknown error")
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
            // New option, default order
            _sortOption.value = option
            _sortOrder.value = if (option == SortOption.NAME) SortOrder.ASCENDING else SortOrder.DESCENDING
        }
    }

    fun loadAssetDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = null // Reset
            val asset = repository.getAssetById(id)
            _detailState.value = asset
        }
    }
    
    fun clearDetail() {
        _detailState.value = null
    }

    fun saveAsset(id: String?, title: String, content: String) {
        viewModelScope.launch {
            repository.saveAsset(id, title, content)
            // List will auto-update via Flow
        }
    }

    fun deleteAsset(id: String) {
        viewModelScope.launch {
            repository.deleteAsset(id)
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
