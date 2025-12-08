package org.example.edvo.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    init {
        checkRegistration()
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
                    _state.value = AuthState.Unlocked
                } else {
                    _state.value = AuthState.Error("Invalid Password")
                    // Reset to Locked state after error to show UI again? 
                    // Or keep Error state and let UI handle "Try Again". 
                    // Better: UI observes Error and user can type again.
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
}
