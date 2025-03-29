package com.evanmccormick.chessevaluator.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ChessEvaluatorApp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    data class LoginUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isLoggedIn: Boolean = false,
        val signInIntent: Intent? = null,
        val isGuestUser: Boolean = false
    )

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Check if user is already logged in
        auth.currentUser?.let {
            _uiState.update { state -> state.copy(isLoggedIn = true) }
        }
    }

    // Email/Password Sign In
    fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password cannot be empty") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                auth.signInWithEmailAndPassword(email, password).await()

                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("wrong password", ignoreCase = true) == true ->
                        "Incorrect password"
                    e.message?.contains("no user record", ignoreCase = true) == true ->
                        "No account found with this email"
                    e.message?.contains("badly formatted", ignoreCase = true) == true ->
                        "Invalid email format"
                    else -> e.message ?: "Authentication failed"
                }

                _uiState.update { it.copy(errorMessage = errorMessage, isLoading = false) }
            }
        }
    }

    // Email/Password Sign Up
    fun createUserWithEmailAndPassword(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password cannot be empty") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                auth.createUserWithEmailAndPassword(email, password).await()

                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("email address is already", ignoreCase = true) == true ->
                        "This email is already in use"
                    e.message?.contains("badly formatted", ignoreCase = true) == true ->
                        "Invalid email format"
                    e.message?.contains("weak password", ignoreCase = true) == true ->
                        "Password is too weak"
                    else -> e.message ?: "Account creation failed"
                }

                _uiState.update { it.copy(errorMessage = errorMessage, isLoading = false) }
            }
        }
    }

    // Function to handle continuing as a guest
    fun continueAsGuest() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Sign in anonymously with Firebase
                auth.signInAnonymously().await()

                // Mark as logged in and as a guest user
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        isGuestUser = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Failed to continue as guest",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createGoogleSignInClient(webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(ChessEvaluatorApp.instance, gso)
    }

    fun onGoogleSignInClick(webClientId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val googleSignInClient = createGoogleSignInClient(webClientId)
                val signInIntent = googleSignInClient.signInIntent

                // We'll return the intent through the state, to be handled by the LoginScreen
                _uiState.update { it.copy(signInIntent = signInIntent) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Google sign-in failed", isLoading = false)
                }
            }
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)

                // Get the ID token from the account
                val idToken = account.idToken

                if (idToken != null) {
                    // Create a credential from the token
                    val credential = GoogleAuthProvider.getCredential(idToken, null)

                    // Sign in with Firebase using the credential
                    auth.signInWithCredential(credential).await()

                    // Update UI state
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = false, signInIntent = null) }
                } else {
                    throw Exception("Google sign-in failed: ID token is null")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Google sign-in failed",
                        isLoading = false,
                        signInIntent = null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSignInIntent() {
        _uiState.update { it.copy(signInIntent = null) }
    }
}