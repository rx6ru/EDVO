package org.example.edvo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.edvo.core.session.SessionManager
import org.example.edvo.domain.repository.AuthRepository

sealed class AuthState {
    object Loading : AuthState()
    object SetupRequired : AuthState()
    object Locked : AuthState()
    object Unlocked : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state = _state.asStateFlow()
    
    // Biometric unlock flag
    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "attr_biometric_enabled"  // Must match SettingsViewModel
    }
    
    init {
        checkRegistration()
        loadBiometricFlag()
    }
    
    private fun loadBiometricFlag() {
        viewModelScope.launch {
            _biometricEnabled.value = authRepository.getFeatureFlag(KEY_BIOMETRIC_ENABLED, false)
        }
    }

    private fun checkRegistration() {
        viewModelScope.launch {
            try {
                val isRegistered = authRepository.isUserRegistered()
                _state.value = if (isRegistered) AuthState.Locked else AuthState.SetupRequired
            } catch (e: Exception) {
                _state.value = AuthState.Error("Failed to check registration: ${e.message}")
            }
        }
    }

    fun register(password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                if (password.isBlank()) {
                    _state.value = AuthState.Error("Password cannot be empty")
                    return@launch
                }
                authRepository.register(password)
                _state.value = AuthState.Unlocked
            } catch (e: Exception) {
                _state.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun login(password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val success = authRepository.login(password)
                if (success) {
                    // If biometric is enabled, store the master key for future biometric unlock
                    if (_biometricEnabled.value) {
                        val masterKey = SessionManager.getMasterKey()
                        if (masterKey != null) {
                            val biometricAuthenticator = BiometricAuthenticator()
                            biometricAuthenticator.storeMasterKey(masterKey)
                        }
                    }
                    _state.value = AuthState.Unlocked
                } else {
                    _state.value = AuthState.Error("Invalid Password")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }
    
    fun resetError() {
         viewModelScope.launch {
             // Re-check state to determine if we act as Locked or Setup
             val isRegistered = authRepository.isUserRegistered()
             _state.value = if (isRegistered) AuthState.Locked else AuthState.SetupRequired
         }
    }
    
    /**
     * Called after successful biometric authentication with the decrypted master key.
     * Starts the session and unlocks the vault.
     */
    fun unlockWithBiometric(masterKey: ByteArray) {
        viewModelScope.launch {
            try {
                // Start the session with the decrypted master key
                SessionManager.startSession(masterKey)
                _state.value = AuthState.Unlocked
            } catch (e: Exception) {
                _state.value = AuthState.Error("Biometric unlock failed: ${e.message}")
            }
        }
    }
    
    /**
     * Show biometric error to user.
     */
    fun showBiometricError(message: String) {
        _state.value = AuthState.Error(message)
    }
}

