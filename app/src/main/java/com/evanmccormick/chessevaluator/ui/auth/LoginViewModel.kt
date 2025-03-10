package com.evanmccormick.chessevaluator.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    // UI State
    data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isLoggedIn: Boolean = false
    )

    // Mutable state (private)
    private val _uiState = MutableStateFlow(LoginUiState())

    // Exposed immutable state
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Event handlers
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onLoginClick() {
        viewModelScope.launch {
            try {
                // Reset error message first
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Simulate API call
                // In a real app, you'd call a repository or usecase
                kotlinx.coroutines.delay(1000)

                // Validate inputs
                val currentState = _uiState.value
                if (currentState.email.isBlank()) {
                    throw Exception("Email cannot be empty")
                }
                if (currentState.password.isBlank()) {
                    throw Exception("Password cannot be empty")
                }

                // Simple validation - in a real app you'd call an auth service
                if (currentState.email.contains("@") && currentState.password.length >= 6) {
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
                } else {
                    throw Exception("Invalid credentials")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }

    fun onGoogleSignInClick() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Simulate Google auth
                // In a real app, you would integrate with Google Sign-In SDK
                kotlinx.coroutines.delay(1000)

                // Simulate successful login
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
