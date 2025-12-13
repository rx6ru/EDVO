package org.example.edvo.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.edvo.domain.repository.AuthRepository
import io.github.vinceglb.filekit.core.PlatformFile
import org.example.edvo.setScreenProtection

sealed class SettingsState {
    object Idle : SettingsState()
    object Loading : SettingsState()
    data class Success(val message: String? = null, val type: OperationType) : SettingsState()
    // DataWiped is a special success case for Kill Switch
    object DataWiped : SettingsState()
    data class Error(val message: String) : SettingsState()
}

enum class OperationType {
    PASSWORD_CHANGE, BACKUP_EXPORT, BACKUP_IMPORT, NONE
}

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Keys matching AuthRepositoryImpl
    private val KEY_SCREENSHOTS = "attr_screenshots"
    private val KEY_COPY_PASTE = "attr_copy_paste"
    private val KEY_SHAKE_TO_LOCK = "attr_shake_to_lock"
    
    // Shake Config Keys
    private val KEY_SHAKE_G_FORCE = "shake_g_force"
    private val KEY_SHAKE_COUNT = "shake_count"
    private val KEY_SHAKE_WINDOW = "shake_window"
    private val KEY_SHAKE_COOLDOWN = "shake_cooldown"

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Idle)
    val state = _state.asStateFlow()

    // Features
    private val _screenshotsEnabled = MutableStateFlow(false) 
    val screenshotsEnabled = _screenshotsEnabled.asStateFlow()
    
    private val _copyPasteEnabled = MutableStateFlow(true)
    val copyPasteEnabled = _copyPasteEnabled.asStateFlow()
    
    private val _shakeToLockEnabled = MutableStateFlow(false)
    val shakeToLockEnabled = _shakeToLockEnabled.asStateFlow()
    
    // Shake Configuration
    private val _shakeConfig = MutableStateFlow(org.example.edvo.core.sensor.ShakeConfig.Default)
    val shakeConfig = _shakeConfig.asStateFlow()
    
    init {
        // Load persisted settings
        viewModelScope.launch {
            val screenshotsIdx = authRepository.getFeatureFlag(KEY_SCREENSHOTS, false)
            val copyPasteIdx = authRepository.getFeatureFlag(KEY_COPY_PASTE, true)
            val shakeToLockIdx = authRepository.getFeatureFlag(KEY_SHAKE_TO_LOCK, false)
            
            _screenshotsEnabled.value = screenshotsIdx
            _copyPasteEnabled.value = copyPasteIdx
            _shakeToLockEnabled.value = shakeToLockIdx
            
            // Load shake config
            loadShakeConfig()
            
            // Enforce security
            setScreenProtection(screenshotsIdx)
        }
    }
    
    private suspend fun loadShakeConfig() {
        val gForce = authRepository.getFeatureFlagFloat(KEY_SHAKE_G_FORCE, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_G_FORCE)
        val count = authRepository.getFeatureFlagInt(KEY_SHAKE_COUNT, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_SHAKE_COUNT)
        val window = authRepository.getFeatureFlagInt(KEY_SHAKE_WINDOW, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_TIME_WINDOW)
        val cooldown = authRepository.getFeatureFlagInt(KEY_SHAKE_COOLDOWN, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_COOLDOWN)
        
        _shakeConfig.value = org.example.edvo.core.sensor.ShakeConfig(
            gForceThreshold = gForce,
            shakeCount = count,
            timeWindow = window,
            cooldown = cooldown
        )
    }
    
    fun updateShakeConfig(
        gForce: Float? = null,
        count: Int? = null,
        window: Int? = null,
        cooldown: Int? = null
    ) {
        viewModelScope.launch {
            val current = _shakeConfig.value
            val updated = current.copy(
                gForceThreshold = gForce ?: current.gForceThreshold,
                shakeCount = count ?: current.shakeCount,
                timeWindow = window ?: current.timeWindow,
                cooldown = cooldown ?: current.cooldown
            )
            _shakeConfig.value = updated
            
            // Persist
            gForce?.let { authRepository.setFeatureFlagFloat(KEY_SHAKE_G_FORCE, it) }
            count?.let { authRepository.setFeatureFlagInt(KEY_SHAKE_COUNT, it) }
            window?.let { authRepository.setFeatureFlagInt(KEY_SHAKE_WINDOW, it) }
            cooldown?.let { authRepository.setFeatureFlagInt(KEY_SHAKE_COOLDOWN, it) }
        }
    }
    
    fun resetShakeConfigToDefaults() {
        viewModelScope.launch {
            _shakeConfig.value = org.example.edvo.core.sensor.ShakeConfig.Default
            authRepository.setFeatureFlagFloat(KEY_SHAKE_G_FORCE, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_G_FORCE)
            authRepository.setFeatureFlagInt(KEY_SHAKE_COUNT, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_SHAKE_COUNT)
            authRepository.setFeatureFlagInt(KEY_SHAKE_WINDOW, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_TIME_WINDOW)
            authRepository.setFeatureFlagInt(KEY_SHAKE_COOLDOWN, org.example.edvo.core.sensor.ShakeConfig.DEFAULT_COOLDOWN)
        }
    }
    
    fun toggleScreenshots(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setFeatureFlag(KEY_SCREENSHOTS, enabled)
            _screenshotsEnabled.value = enabled
            setScreenProtection(enabled)
        }
    }
    
    fun toggleCopyPaste(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setFeatureFlag(KEY_COPY_PASTE, enabled)
            _copyPasteEnabled.value = enabled
        }
    }
    
    fun toggleShakeToLock(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setFeatureFlag(KEY_SHAKE_TO_LOCK, enabled)
            _shakeToLockEnabled.value = enabled
        }
    }

    fun changePassword(old: String, new: String) {
        viewModelScope.launch {
            _state.value = SettingsState.Loading
            try {
                authRepository.changePassword(old, new)
                _state.value = SettingsState.Success(type = OperationType.PASSWORD_CHANGE)
            } catch (e: Exception) {
                _state.value = SettingsState.Error(e.message ?: "Failed to change password")
            }
        }
    }

    fun wipeVault() {
        // Deprecated simple wipe
    }

    private val _exportData = MutableStateFlow<ByteArray?>(null)
    val exportData = _exportData.asStateFlow()

    fun exportBackup(password: String) {
        viewModelScope.launch {
            _state.value = SettingsState.Loading
            try {
                val backupBytes = authRepository.exportBackup(password)
                _exportData.value = backupBytes
                _state.value = SettingsState.Success("Backup Exported Successfully", OperationType.BACKUP_EXPORT)
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
            _state.value = SettingsState.Loading
            try {
                val bytes = file.readBytes()
                authRepository.importBackup(password, bytes)
                _state.value = SettingsState.Success("Backup Imported Successfully", OperationType.BACKUP_IMPORT)
            } catch (e: Exception) {
                _state.value = SettingsState.Error("Import failed: ${e.message}")
            }
        }
    }

    fun triggerKillSwitch(password: String) {
        viewModelScope.launch {
            _state.value = SettingsState.Loading
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
