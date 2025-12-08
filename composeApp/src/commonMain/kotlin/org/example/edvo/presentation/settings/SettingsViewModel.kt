package org.example.edvo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.edvo.domain.repository.AuthRepository
import io.github.vinceglb.filekit.core.PlatformFile

sealed class SettingsState {
    object Idle : SettingsState()
    object Loading : SettingsState()
    object Submitting : SettingsState()
    object Success : SettingsState()
    object DataWiped : SettingsState()
    data class Error(val message: String) : SettingsState()
}

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Idle)
    val state = _state.asStateFlow()

    fun changePassword(old: String, new: String) {
        viewModelScope.launch {
            _state.value = SettingsState.Submitting
            try {
                authRepository.changePassword(old, new)
                _state.value = SettingsState.Success
            } catch (e: Exception) {
                _state.value = SettingsState.Error(e.message ?: "Failed to change password")
            }
        }
    }

    fun wipeVault() {
        // Deprecated simple wipe, redirect to killSwitch? 
        // Or keep internal.
        // User wants Kill Switch button with password.
        // This function was used by previous "Wipe Vault". 
        // Note: verifyPassword calls are async.
    }

    private val _exportData = MutableStateFlow<ByteArray?>(null)
    val exportData = _exportData.asStateFlow()

    fun exportBackup(password: String) {
        viewModelScope.launch {
            _state.value = SettingsState.Submitting
            try {
                val backupBytes = authRepository.exportBackup(password)
                _exportData.value = backupBytes
                _state.value = SettingsState.Success
            } catch (e: Exception) {
                _state.value = SettingsState.Error("Export failed: ${e.message}")
            }
        }
    }
    
    fun clearExportData() {
        _exportData.value = null
    }
    
    fun importBackup(password: String, file: io.github.vinceglb.filekit.core.PlatformFile) {
        viewModelScope.launch {
            _state.value = SettingsState.Submitting
            try {
                val bytes = file.readBytes()
                authRepository.importBackup(password, bytes)
                _state.value = SettingsState.Success
            } catch (e: Exception) {
                _state.value = SettingsState.Error("Import failed: ${e.message}")
            }
        }
    }

    fun triggerKillSwitch(password: String) {
        viewModelScope.launch {
            _state.value = SettingsState.Submitting
            try {
                if (authRepository.verifyPassword(password)) {
                    authRepository.wipeData()
                    _state.value = SettingsState.DataWiped
                } else {
                    _state.value = SettingsState.Error("Invalid Password")
                }
            } catch (e: Exception) {
                _state.value = SettingsState.Error(e.message ?: "Failed to wipe data")
            }
        }
    }
    
    fun resetState() {
        _state.value = SettingsState.Idle
    }
}
